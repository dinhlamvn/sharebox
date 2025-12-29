package com.dinhlam.sharebox.ui.downloadpopup

import android.content.Context
import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.downloader.Downloader
import com.dinhlam.sharebox.extensions.asHumanReadableSize
import com.dinhlam.sharebox.extensions.ext
import com.dinhlam.sharebox.extensions.getFileNameAndSize
import com.dinhlam.sharebox.extensions.getMimeTypeFromUri
import com.dinhlam.sharebox.extensions.isAudioMimeType
import com.dinhlam.sharebox.extensions.isImageMimeType
import com.dinhlam.sharebox.extensions.isImageUrl
import com.dinhlam.sharebox.extensions.isLocalUri
import com.dinhlam.sharebox.extensions.isNetworkUrl
import com.dinhlam.sharebox.extensions.isVideoMimeType
import com.dinhlam.sharebox.extensions.mimeType
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
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
import androidx.core.net.toUri

@HiltViewModel
class BottomSheetDownloadViewModel @Inject constructor(
    private val videoHelper: VideoHelper,
    private val okHttpClient: OkHttpClient,
    @param:Named("TiktokDownloaderV2") private val tiktokDownloader: Downloader,
    @param:Named("FacebookDownloaderV2") private val facebookDownloader: Downloader,
    @param:Named("YoutubeDownloader") private val youtubeDownloader: Downloader,
) : BaseViewModel<BottomSheetDownloadState>(BottomSheetDownloadState()) {

    fun download(context: Context, downloadLinks: List<String>) {
        suspend {
            val videos = mutableListOf<DownloadData>()
            val images = mutableListOf<DownloadData>()
            val audios = mutableListOf<DownloadData>()
            val files = mutableListOf<DownloadData>()
            val links = downloadLinks.distinct()
            repeat(links.size) { idx ->
                val downloadUrl = links[idx]
                val downloadContent = if (downloadUrl.isNetworkUrl()) {
                    val originUrlPair = getOriginUrl(downloadUrl)
                    downloadNetworkFile(originUrlPair.first, originUrlPair.second)
                } else if (downloadUrl.isLocalUri()) {
                    downloadLocalFile(context, downloadUrl)
                } else {
                    DownloadContent()
                }
                videos.addAll(downloadContent.videos)
                images.addAll(downloadContent.images)
                audios.addAll(downloadContent.audios)
                files.addAll(downloadContent.files)
            }
            DownloadContent(videos, audios, images, files)
        }.execute { asyncLoad ->
            copy(asyncLoadDownload = asyncLoad)
        }
    }

    private fun downloadLocalFile(context: Context, url: String): DownloadContent {
        val uri = url.toUri()
        val mimeType = context.getMimeTypeFromUri(uri) ?: return DownloadContent()
        val pair = context.getFileNameAndSize(uri)
        return when {
            mimeType.isImageMimeType() -> {
                val images = listOf(
                    DownloadData(
                        "image_$url",
                        mimeType,
                        "(${pair.second.asHumanReadableSize()})",
                        url
                    )
                )
                DownloadContent(images = images)
            }

            mimeType.isVideoMimeType() -> {
                val videos = listOf(
                    DownloadData(
                        "video_$url",
                        mimeType,
                        "(${pair.second.asHumanReadableSize()})",
                        url
                    )
                )
                DownloadContent(videos = videos)
            }

            mimeType.isAudioMimeType() -> {
                val audios = listOf(
                    DownloadData(
                        "audio_$url",
                        mimeType,
                        "(${pair.second.asHumanReadableSize()})",
                        url
                    )
                )
                DownloadContent(audios = audios)
            }

            else -> {
                val files = listOf(
                    DownloadData(
                        "file_$url",
                        mimeType,
                        "(${pair.second.asHumanReadableSize()})",
                        url
                    )
                )
                DownloadContent(files = files)
            }
        }
    }

    private suspend fun downloadNetworkFile(url: String, mimeType: String?): DownloadContent {
        if (url.isImageUrl()) {
            return downloadImage(url)
        }
        return try {
            downloadVideo(url)
        } catch (e: Exception) {
            Logger.error("Download video has error: $e")
            downloadFromUrl(url, mimeType)
        }
    }

    private fun downloadFromUrl(url: String, mimeType: String?): DownloadContent {
        val correctMimeType = mimeType ?: return DownloadContent()
        return when {
            correctMimeType.isImageMimeType() -> {
                val images = listOf(
                    DownloadData(
                        "image_$url",
                        correctMimeType,
                        "(${correctMimeType.uppercase()})",
                        url
                    )
                )
                DownloadContent(images = images)
            }

            correctMimeType.isVideoMimeType() -> {
                val videos = listOf(
                    DownloadData(
                        "video_$url",
                        correctMimeType,
                        "(${correctMimeType.uppercase()})",
                        url
                    )
                )
                DownloadContent(videos = videos)
            }

            correctMimeType.isAudioMimeType() -> {
                val audios = listOf(
                    DownloadData(
                        "audio_$url",
                        correctMimeType,
                        "(${correctMimeType.uppercase()})",
                        url
                    )
                )
                DownloadContent(audios = audios)
            }

            else -> {
                val files = listOf(
                    DownloadData(
                        "file_$url",
                        correctMimeType,
                        "(${correctMimeType.uppercase()})",
                        url
                    )
                )
                DownloadContent(files = files)
            }
        }
    }

    private fun downloadImage(url: String): DownloadContent {
        val ext = url.ext ?: return DownloadContent()
        val mimetype = url.mimeType ?: return DownloadContent()
        val images = listOf(DownloadData("image_$url", mimetype, "(${ext.uppercase()})", url))
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
        val videos = listOf(DownloadData("video_$url", mimetype, "(${ext.uppercase()})", url))
        return DownloadContent(videos = videos)
    }

    private suspend fun getOriginUrl(s: String): Pair<String, String?> =
        withContext(Dispatchers.IO) {
            val call = okHttpClient.newCall(Request.Builder().url(s).build())
            val body = call.execute()

            if (body.isRedirect) {
                return@withContext body.use { responseBody ->
                    val url = responseBody.header("Location")!!
                    val mimeType = responseBody.header("Content-Type")?.substringBefore(";")
                    Pair(url, mimeType)
                }
            }

            val pair = body.use { responseBody ->
                val url = responseBody.request.url.toString()
                val mimeType = responseBody.header("Content-Type")?.substringBefore(";")
                Pair(url, mimeType)
            }

            if ((pair.first.contains("fb.com") || pair.first.contains("facebook.com")) && pair.first.contains(
                    "/login"
                )
            ) {
                Pair(pair.first.toUri().getQueryParameter("next")!!, pair.second)
            } else {
                pair
            }
        }
}