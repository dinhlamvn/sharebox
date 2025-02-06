package com.dinhlam.sharebox.downloader

import android.net.Uri
import com.dinhlam.sharebox.data.network.GetMyFBServices
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.FacebookDownloadErrorEvent
import javax.inject.Inject

class FacebookDownloaderV2 @Inject constructor(
    private val videoHelper: VideoHelper,
    private val getMyFBServices: GetMyFBServices,
) : Downloader {

    override suspend fun download(downloadUrl: String): DownloadContent {
        return try {
            val facebookUrl = videoHelper.getFacebookUrl(downloadUrl)
            val videoId =
                Uri.parse(facebookUrl).lastPathSegment ?: error("No video id")
            val response = getMyFBServices.fetch(facebookUrl)
            val videos = listOf(
                DownloadData(
                    videoId, "video/mp4", "SD", response.sd
                ),
                DownloadData(
                    videoId, "video/mp4", "HD", response.hd
                )
            )
            return DownloadContent(videos, emptyList(), emptyList())
        } catch (e: Exception) {
            TrackerManager.logEvent(FacebookDownloadErrorEvent(e.message))
            DownloadContent()
        }
    }
}