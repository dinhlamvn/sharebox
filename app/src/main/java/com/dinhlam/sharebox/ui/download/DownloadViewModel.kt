package com.dinhlam.sharebox.ui.download

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.downloader.Downloader
import com.dinhlam.sharebox.extensions.ext
import com.dinhlam.sharebox.extensions.isImageUrl
import com.dinhlam.sharebox.extensions.mimeType
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.model.VideoSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val videoHelper: VideoHelper,
    private val okHttpClient: OkHttpClient,
    @Named("TiktokDownloaderV2") private val tiktokDownloader: Downloader,
    @Named("FacebookDownloaderV2") private val facebookDownloader: Downloader,
    @Named("YoutubeDownloader") private val youtubeDownloader: Downloader,
) : BaseViewModel<DownloadState>(DownloadState()) {

    fun download(downloadLink: String) {
        suspend {
            val originUrl = getOriginUrl(downloadLink)
            downloadInternal(originUrl)
        }.execute { asyncLoad ->
            copy(asyncLoadDownload = asyncLoad)
        }
    }

    private suspend fun downloadInternal(url: String): DownloadContent {
        if (url.isImageUrl()) {
            return downloadImage(url)
        }
        return downloadVideo(url)
    }

    private fun downloadImage(url: String): DownloadContent {
        val ext = url.ext ?: return DownloadContent()
        val mimetype = url.mimeType ?: return DownloadContent()
        val images = listOf(DownloadData("image", mimetype, "(${ext.uppercase()})", url))
        return DownloadContent(images = images)
    }

    private suspend fun downloadVideo(url: String): DownloadContent {
        val videoSource = videoHelper.getVideoSource(url) ?: error("No video source")
        val videoOriginUrl =
            videoHelper.getVideoOriginUrl(videoSource, url) ?: error("No video url")
        return downloadVideo(videoSource, videoOriginUrl)
    }

    private suspend fun downloadVideo(
        videoSource: VideoSource,
        downloadUrl: String
    ): DownloadContent {
        return when (videoSource) {
            VideoSource.Directly -> downloadVideoDirectly(downloadUrl)
            VideoSource.Tiktok -> tiktokDownloader.download(downloadUrl)
            VideoSource.Youtube -> youtubeDownloader.download(downloadUrl)
            VideoSource.Facebook -> facebookDownloader.download(downloadUrl)
        }
    }

    private fun downloadVideoDirectly(url: String): DownloadContent {
        val ext = url.ext ?: return DownloadContent()
        val mimetype = url.mimeType ?: return DownloadContent()
        val videos = listOf(DownloadData("video", mimetype, "(${ext.uppercase()})", url))
        return DownloadContent(videos = videos)
    }

    private suspend fun getOriginUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.use { responseBody -> responseBody.header("Location")!! }
        }

        val url = body.use { responseBody -> responseBody.request.url.toString() }

        if ((url.contains("fb.com") || url.contains("facebook.com")) && url.contains("/login")) {
            Uri.parse(url).getQueryParameter("next")!!
        } else {
            url
        }
    }
}