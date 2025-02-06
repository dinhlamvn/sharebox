package com.dinhlam.sharebox.helper

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.ext
import com.dinhlam.sharebox.extensions.isFacebookVideo
import com.dinhlam.sharebox.extensions.isTiktokVideo
import com.dinhlam.sharebox.extensions.isVideoUrl
import com.dinhlam.sharebox.extensions.isYoutubeVideo
import com.dinhlam.sharebox.model.AppSettings
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
    private val networkHelper: NetworkHelper,
    private val appSettingHelper: AppSettingHelper,
) {
    fun getVideoSource(url: String): VideoSource? {
        return when {
            url.isVideoUrl() -> VideoSource.Directly
            url.isYoutubeVideo() -> VideoSource.Youtube
            url.isTiktokVideo() -> VideoSource.Tiktok
            url.isFacebookVideo() -> VideoSource.Facebook
            else -> null
        }
    }

    suspend fun getTiktokUrl(url: String): String {
        val fullUrl = getTiktokFullUrl(url)
        val uri = Uri.parse(fullUrl)
        return Uri.decode(
            Uri.Builder().scheme(uri.scheme).authority(uri.authority).path(uri.path)
                .fragment(uri.fragment).build().toString()
        )
    }

    private suspend fun getTiktokFullUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.use { it.header("Location")!! }
        }

        body.use { responseBody -> responseBody.request.url.toString() }
    }

    suspend fun getFacebookUrl(url: String): String {
        val fullUrl = getFullFacebookUrl(url)
        val uri = Uri.parse(fullUrl)
        return Uri.decode(
            Uri.Builder().scheme(uri.scheme).authority(uri.authority).path(uri.path)
                .fragment(uri.fragment).build().toString()
        )
    }

    private suspend fun getFullFacebookUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.use { it.header("Location")!! }
        }

        val url = body.use { it.request.url.toString() }

        if (url.contains("/login")) {
            Uri.parse(url).getQueryParameter("next").orEmpty()
        } else {
            url
        }
    }

    suspend fun getVideoOriginUrl(videoSource: VideoSource, url: String): String? {
        return try {
            when (videoSource) {
                VideoSource.Tiktok -> getTiktokUrl(url)
                VideoSource.Facebook -> getFullFacebookUrl(url)
                VideoSource.Youtube -> url
                else -> url
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadVideo(
        context: Context,
        id: Int,
        videoSource: VideoSource,
        videoUri: String
    ) {
        if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.network_wifi_only_warning, Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }

        when (videoSource) {
            VideoSource.Tiktok -> WorkerUtils.enqueueJobDownloadTiktokVideo(context, id, videoUri)
            VideoSource.Youtube -> WorkerUtils.enqueueJobDownloadYoutube(context, id, videoUri)
            VideoSource.Facebook -> WorkerUtils.enqueueJobDownloadFacebookVideo(
                context,
                id,
                videoUri
            )

            else -> videoUri.ext?.let { fileExt ->
                DownloadHelper.enqueueDownload(
                    context,
                    videoUri,
                    "sharebox_video_${id}_${System.currentTimeMillis()}.$fileExt"
                )
            }
        }
    }
}