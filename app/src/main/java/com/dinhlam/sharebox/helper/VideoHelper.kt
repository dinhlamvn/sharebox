package com.dinhlam.sharebox.helper

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.data.network.LoveTikServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoHelper @Inject constructor(
    private val loveTikServices: LoveTikServices, private val okHttpClient: OkHttpClient
) {

    fun getVideoSource(url: String): VideoSource {
        return when {
            isYoutubeVideo(url) -> VideoSource.Youtube
            isTiktokVideo(url) -> VideoSource.Tiktok
            isFacebookVideo(url) -> VideoSource.Facebook
            else -> error("No source found for url $url")
        }
    }

    suspend fun getVideoSourceId(videoSource: VideoSource, url: String): String {
        return when (videoSource) {
            is VideoSource.Youtube -> getYoutubeVideoSourceId(url)
            is VideoSource.Tiktok -> getTiktokVideoSourceId(url)
            is VideoSource.Facebook -> getFacebookVideoSourceId(url)
            else -> error("No source id found for url $url - video source: $videoSource")
        }
    }

    suspend fun getVideoUri(context: Context, videoSource: VideoSource, url: String): Uri? {
        return when (videoSource) {
            is VideoSource.Youtube -> null
            is VideoSource.Tiktok -> getTiktokVideoUri(context, url)
            is VideoSource.Facebook -> null
            else -> null
        }
    }

    private suspend fun getTiktokVideoUri(context: Context, url: String): Uri? =
        withContext(Dispatchers.IO) {
            val tiktokUrl = formatUrlForTiktok(url)
            val response = loveTikServices.getVideoDownloadUrl(tiktokUrl)
            val link =
                response?.links?.firstOrNull { it.watermarkInfo == "Watermarked" }?.downloadLink
                    ?: return@withContext null
            val outputDir = File(context.filesDir, "tiktok_videos")
            if (!outputDir.exists() && !outputDir.mkdir()) {
                return@withContext null
            }
            val outputFile = File(outputDir, "${response.videoId}.mp4")
            if (outputFile.exists()) {
                if (outputFile.length() > 0L) {
                    return@withContext FileProvider.getUriForFile(
                        context, "${BuildConfig.APPLICATION_ID}.file_provider", outputFile
                    )
                }
                outputFile.delete()
            }

            if (!outputFile.createNewFile()) {
                return@withContext null
            }
            downloadVideoTiktok(link, outputFile)
            FileProvider.getUriForFile(
                context, "${BuildConfig.APPLICATION_ID}.file_provider", outputFile
            )
        }

    private suspend fun formatUrlForTiktok(url: String): String {
        val fullUrl = getFullTiktokUrl(url)
        val uri = Uri.parse(fullUrl)
        return Uri.decode(
            Uri.Builder().scheme(uri.scheme).authority(uri.authority).path(uri.path)
                .fragment(uri.fragment).build().toString()
        )
    }

    private suspend fun getFullTiktokUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.header("Location")!!
        }

        body.request().url().url().toString()
    }

    private fun getYoutubeVideoSourceId(url: String): String {
        val uri = Uri.parse(url)
        return if (url.contains("/shorts/") || url.contains("youtu.be") || url.contains("youtube.com/live/")) {
            uri.lastPathSegment!!
        } else {
            uri.getQueryParameter("v")!!
        }
    }

    private suspend fun getTiktokVideoSourceId(url: String): String {
        val tiktokUrl = formatUrlForTiktok(url)
        return Uri.parse(tiktokUrl).lastPathSegment!!
    }

    private suspend fun getFacebookVideoSourceId(url: String): String {
        val originalUri = Uri.parse(url)
        if (originalUri.path?.contains("videos") == true || originalUri.path?.contains("reel") == true) {
            return originalUri.lastPathSegment!!
        }
        val fullUrl = getFullFacebookUrl(url)
        if (!fullUrl.contains("/videos/") && !fullUrl.contains("/reel/")) {
            error("Facebook $url isn't contain videos in path")
        }
        return Uri.parse(fullUrl).lastPathSegment!!
    }

    private suspend fun getFullFacebookUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.header("Location")!!
        }

        val url = body.request().url().url().toString()

        if (url.contains("login.php?next=")) {
            Uri.parse(url).getQueryParameter("next")!!
        } else {
            url
        }
    }

    private fun isYoutubeVideo(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun isTiktokVideo(url: String): Boolean {
        return url.contains("tiktok.com")
    }

    private fun isFacebookVideo(url: String): Boolean {
        return (url.contains(Regex("facebook.com|fb.com|fb.watch")) && (url.contains("watch") || url.contains(
            "/videos/"
        ) || url.contains("reel"))) || url.contains("fb.gg/v/")
    }

    private suspend fun downloadVideoTiktok(url: String, output: File) {
        val response = loveTikServices.downloadFile(url)
        response.byteStream().use { stream ->
            FileOutputStream(output).use { outputStream ->
                stream.copyTo(outputStream)
            }
        }
    }
}