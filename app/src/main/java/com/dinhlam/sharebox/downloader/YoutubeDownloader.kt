package com.dinhlam.sharebox.downloader

import android.net.Uri
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.YoutubeDownloadErrorEvent
import com.dinhlam.sharebox.utils.UserAgentUtils
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class YoutubeDownloader @Inject constructor(
    private val libreTubeServices: LibreTubeServices,
) : Downloader {
    override suspend fun download(downloadUrl: String): DownloadContent {
        return try {
            val videoId = getYoutubeVideoId(downloadUrl) ?: return DownloadContent()
            var retry = 3
            var strResponse = ""
            while (retry > 0) {
                val responseBody = libreTubeServices.getDownloadLink(videoId)
                if (!responseBody.isSuccessful) {
                    val error = "${responseBody.code()} - " + responseBody.errorBody()?.string()
                    TrackerManager.logEvent(YoutubeDownloadErrorEvent(error))
                    delay(1000)
                    retry--
                    continue
                }

                strResponse =
                    responseBody.body()?.use { res -> res.string() }.orEmpty()

                if (strResponse.isNotEmpty()) {
                    break
                }
                delay(1000)
                retry--
            }

            if (strResponse.isEmpty()) {
                return DownloadContent()
            }

            val json = JSONObject(strResponse)
            val videoStreams = json.getJSONArray("videoStreams") ?: JSONArray()
            val audioStreams = json.getJSONArray("audioStreams") ?: JSONArray()

            if (videoStreams.length() == 0 && audioStreams.length() == 0) {
                return DownloadContent()
            }

            val videos = mutableListOf<DownloadData>()
            for (i in 0 until videoStreams.length()) {
                val videoObj = videoStreams.getJSONObject(i)
                val mimeType = videoObj.getString("mimeType")
                val isVideoOnly = videoObj.getBoolean("videoOnly")
                val videoQuality = videoObj.getString("quality")
                if (mimeType.contains("video/mp4", true)) {
                    val suffix = if (isVideoOnly) {
                        "($videoQuality) - No Sound"
                    } else {
                        "($videoQuality})"
                    }
                    videos.add(
                        DownloadData(
                            videoId, mimeType, suffix, videoObj.getString("url")
                        )
                    )
                }
            }

            val audios = mutableListOf<DownloadData>()
            for (i in 0 until audioStreams.length()) {
                val audioObject = audioStreams.getJSONObject(i)
                val mimeType = audioObject.getString("mimeType")
                if (mimeType.contains("audio/mp3", true) || mimeType.contains("audio/mp4", true)) {
                    audios.add(
                        DownloadData(
                            videoId,
                            mimeType,
                            "(${audioObject.getString("quality")})",
                            audioObject.getString("url")
                        )
                    )
                }
            }

            return DownloadContent(videos, audios, emptyList())
        } catch (e: Exception) {
            TrackerManager.logEvent(YoutubeDownloadErrorEvent(e.message))
            DownloadContent()
        }
    }

    private fun getYoutubeVideoId(sourceUrl: String): String? {
        val uri = Uri.parse(sourceUrl)
        if (sourceUrl.contains("/shorts/")) {
            return uri.lastPathSegment
        }
        return uri.getQueryParameter("v")
    }
}