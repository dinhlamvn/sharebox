package com.dinhlam.sharebox.helper

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.utils.WorkerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoHelper @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val videoMixerRepository: VideoMixerRepository,
) {
    fun getVideoSource(url: String): VideoSource? {
        return when {
            isYoutubeVideo(url) -> VideoSource.Youtube
            isTiktokVideo(url) -> VideoSource.Tiktok
            isFacebookVideo(url) -> VideoSource.Facebook
            else -> null
        }
    }

    private suspend fun getVideoSourceId(videoSource: VideoSource, url: String): String {
        return when (videoSource) {
            is VideoSource.Youtube -> getYoutubeVideoSourceId(url)
            is VideoSource.Tiktok -> getTiktokVideoSourceId(url)
            is VideoSource.Facebook -> getFacebookVideoSourceId(url)
        }
    }

    suspend fun getTiktokFullUrl(url: String): String {
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

    private suspend fun getVideoOriginUrl(videoSource: VideoSource, url: String): String {
        return when (videoSource) {
            VideoSource.Tiktok -> getTiktokFullUrl(url)
            VideoSource.Facebook -> getFullFacebookUrl(url)
            VideoSource.Youtube -> url
        }
    }

    suspend fun syncVideo(shareId: String, shareUrl: String) {
        videoMixerRepository.findOne(shareId) ?: run {
            val videoSource = getVideoSource(shareUrl) ?: return
            val videoSourceId = getVideoSourceId(videoSource, shareUrl)
            val videoOriginUrl = getVideoOriginUrl(videoSource, shareUrl)
            videoMixerRepository.insert(shareId, videoOriginUrl, videoSource, videoSourceId)
        }
    }

    fun downloadVideo(context: Context, id: Int, videoSource: VideoSource, videoUri: String) {
        when (videoSource) {
            VideoSource.Tiktok -> WorkerUtils.enqueueJobDownloadTiktokVideo(context, id, videoUri)
            VideoSource.Youtube -> WorkerUtils.enqueueJobDownloadYoutubeMp3(context, id, videoUri)
            else -> Toast.makeText(context, R.string.can_not_save_video, Toast.LENGTH_SHORT).show()
        }
    }
}