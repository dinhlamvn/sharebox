package com.dinhlam.sharebox.helper

import android.content.Context
import android.net.Uri
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.data.network.LoveTikServices
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoHelper @Inject constructor(
    private val loveTikServices: LoveTikServices,
    private val okHttpClient: OkHttpClient,
    private val sssTikService: SSSTikServices,
) {

    fun getVideoSource(url: String): VideoSource? {
        return when {
            isYoutubeVideo(url) -> VideoSource.Youtube
            isTiktokVideo(url) -> VideoSource.Tiktok
            isFacebookVideo(url) -> VideoSource.Facebook
            else -> null
        }
    }

    suspend fun getVideoSourceId(videoSource: VideoSource, url: String): String {
        return when (videoSource) {
            is VideoSource.Youtube -> getYoutubeVideoSourceId(url)
            is VideoSource.Tiktok -> getTiktokVideoSourceId(url)
            is VideoSource.Facebook -> getFacebookVideoSourceId(url)
        }
    }

    suspend fun getVideoUri(context: Context, videoSource: VideoSource, url: String): Uri? {
        return when (videoSource) {
            is VideoSource.Youtube -> null
            is VideoSource.Tiktok -> getTiktokVideoUri(context, url)
            is VideoSource.Facebook -> null
        }
    }

    private suspend fun getTiktokVideoUri(context: Context, url: String): Uri? =
        withContext(Dispatchers.IO) {
            val tiktokUrl = getTiktokFullUrl(url)
            val videoId = Uri.parse(tiktokUrl).lastPathSegment ?: return@withContext null
            var retryTimes = 3
            var html: String = ""
            do {
                val encodeUrl = URLEncoder.encode(tiktokUrl, "utf-8")
                val sssTikId = "$encodeUrl&locale=en&tt=azhwU005"
                val requestBody = RequestBody.create(MediaType.parse("text/plain"), "id=$sssTikId")
                val sssTikResponse = sssTikService.getDownloadLink(requestBody)
                if (!sssTikResponse.isSuccessful) {
                    retryTimes--
                    delay(1000)
                    continue
                }
                html = sssTikResponse.body()?.use { responseBody -> responseBody.use { it.string() } } ?: ""

                if (html.isNotEmpty()) {
                    break
                }

                retryTimes--
                delay(1000)
            } while (retryTimes > 0)

            if (html.isEmpty()) {
                return@withContext null
            }

            val downloadUrl = parseHtmlSSSTik(html) ?: return@withContext null

            val outputDir = File(context.filesDir, "tiktok_videos")
            if (!outputDir.exists() && !outputDir.mkdir()) {
                return@withContext null
            }
            val outputFile = File(outputDir, "$videoId.mp4")
            if (outputFile.exists()) {
                if (outputFile.length() > 0L) {
                    return@withContext FileUtils.getUriFromFile(context, outputFile)
                }
                outputFile.delete()
            }

            if (!outputFile.createNewFile()) {
                return@withContext null
            }
            downloadVideoTiktok(downloadUrl, outputFile)
            FileUtils.getUriFromFile(context, outputFile)
        }

    private suspend fun getTiktokFullUrl(url: String): String {
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
            return@withContext body.use { it.header("Location")!! }
        }

        body.use { it.request().url().url().toString() }
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
        val tiktokUrl = getTiktokFullUrl(url)
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
            return@withContext body.use { it.header("Location")!! }
        }

        val url = body.use { it.request().url().url().toString() }

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
        response.use { res ->
            res.byteStream().use { stream ->
                FileOutputStream(output).use { outputStream ->
                    stream.copyTo(outputStream)
                }
            }
        }
    }

    private fun parseHtmlSSSTik(htmlString: String): String? {
        val jsoup = Jsoup.parse(htmlString)
        val aTagDownload = jsoup.getElementById("direct_dl_link") ?: return null
        if (!aTagDownload.hasAttr("href")) {
            return null
        }
        return aTagDownload.attr("href")
    }
}