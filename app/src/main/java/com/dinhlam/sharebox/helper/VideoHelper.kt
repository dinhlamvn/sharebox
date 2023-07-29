package com.dinhlam.sharebox.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.LoveTikServices
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.saveFile
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.UserAgentUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.Jsoup
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoHelper @Inject constructor(
    private val loveTikServices: LoveTikServices,
    private val okHttpClient: OkHttpClient,
    private val sssTikService: SSSTikServices,
    private val videoMixerRepository: VideoMixerRepository,
    private val localStorageHelper: LocalStorageHelper,
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

    private suspend fun getVideoUri(
        context: Context,
        id: Int,
        videoSource: VideoSource,
        url: String,
        onComplete: suspend (Uri?) -> Unit
    ) {
        when (videoSource) {
            is VideoSource.Youtube -> onComplete.invoke(null)
            is VideoSource.Tiktok -> getTiktokVideoUri(context, id, url, onComplete)
            is VideoSource.Facebook -> onComplete.invoke(null)
        }
    }

    suspend fun saveVideo(
        context: Context,
        id: Int,
        videoSource: VideoSource,
        url: String,
        onResult: (Boolean) -> Unit
    ) {
        getVideoUri(context, id, videoSource, url) { uri ->
            uri?.let { nonNullUri ->
                localStorageHelper.saveVideoToGallery(nonNullUri)
                localStorageHelper.cleanUp(nonNullUri)
                onResult.invoke(true)
            } ?: onResult.invoke(false)
        }
    }

    private suspend fun getTiktokVideoUri(
        context: Context, id: Int, url: String, onComplete: suspend (Uri?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val notificationBuilder =
            NotificationCompat.Builder(context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(context.getString(R.string.download_video_subtext))
                .setAutoCancel(true).setContentTitle(context.getString(R.string.download_video))
                .setSmallIcon(R.drawable.ic_download).setProgress(0, 0, true)

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notificationBuilder.build())
        }


        val tiktokUrl = getTiktokFullUrl(url)
        val videoId = Uri.parse(tiktokUrl).lastPathSegment
            ?: return@withContext NotificationManagerCompat.from(context).cancel(id)
        var retryTimes = 3
        var html: String = ""
        do {
            val encodeUrl = URLEncoder.encode(tiktokUrl, "utf-8")
            val sssTikId = "$encodeUrl&locale=en&tt=azhwU005"
            val requestBody = RequestBody.create(MediaType.parse("text/plain"), "id=$sssTikId")
            val sssTikResponse =
                sssTikService.getDownloadLink(UserAgentUtils.pickRandomUserAgent(), requestBody)
            if (!sssTikResponse.isSuccessful) {
                retryTimes--
                delay(1000)
                continue
            }
            html = sssTikResponse.body()?.use { responseBody -> responseBody.use { it.string() } }
                ?: ""

            if (html.isNotEmpty()) {
                break
            }

            retryTimes--
            delay(1000)
        } while (retryTimes > 0)

        if (html.isEmpty()) {
            return@withContext NotificationManagerCompat.from(context).cancel(id)
        }

        val downloadUrl =
            parseHtmlSSSTik(html) ?: return@withContext NotificationManagerCompat.from(context)
                .cancel(id)

        val outputDir = File(context.filesDir, "tiktok_videos")
        if (!outputDir.exists() && !outputDir.mkdir()) {
            return@withContext NotificationManagerCompat.from(context).cancel(id)
        }
        val outputFile = File(outputDir, "$videoId.mp4")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        if (!outputFile.createNewFile()) {
            return@withContext NotificationManagerCompat.from(context).cancel(id)
        }

        downloadVideoTiktok(context, id, downloadUrl, outputFile).collect { downloadState ->
            when (downloadState) {
                is DownloadState.Downloading -> {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        NotificationManagerCompat.from(context).notify(
                            id,
                            notificationBuilder.setProgress(100, downloadState.progress, false)
                                .build()
                        )
                    }
                }

                is DownloadState.Finished -> {
                    NotificationManagerCompat.from(context).cancel(id)
                    onComplete.invoke(FileUtils.getUriFromFile(context, outputFile))
                }

                is DownloadState.Failed -> {
                    NotificationManagerCompat.from(context).cancel(id)
                }
            }
        }
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

    private suspend fun downloadVideoTiktok(
        context: Context, id: Int, url: String, output: File
    ): Flow<DownloadState> {
        return loveTikServices.downloadFile(url).saveFile(output)
    }

    private fun parseHtmlSSSTik(htmlString: String): String? {
        val jsoup = Jsoup.parse(htmlString)
        val aTags = jsoup.getElementsByTag("a")
        return aTags.firstOrNull { element ->
            if (!element.hasAttr("href")) {
                return null
            }
            val href = element.attr("href") ?: ""
            val isMp4Download = element.text().contains("Without watermark")
            href.contains("tikcdn.io") && isMp4Download
        }?.attr("href").takeIfNotNullOrBlank()
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
}