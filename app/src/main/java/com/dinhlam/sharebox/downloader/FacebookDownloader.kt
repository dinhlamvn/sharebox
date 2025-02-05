package com.dinhlam.sharebox.downloader

import android.net.Uri
import com.dinhlam.sharebox.data.network.FDownServices
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.FacebookDownloadErrorEvent
import com.dinhlam.sharebox.utils.UserAgentUtils
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.util.Locale
import javax.inject.Inject

class FacebookDownloader @Inject constructor(
    private val videoHelper: VideoHelper,
    private val fDownServices: FDownServices,
) : Downloader {

    override suspend fun download(downloadUrl: String): DownloadContent {
        return try {
            val facebookUrl = videoHelper.getFacebookUrl(downloadUrl)
            val videoId =
                Uri.parse(facebookUrl).lastPathSegment ?: error("No video id")
            var retryTimes = 3
            var html = ""
            do {
                val downloadResponse = fDownServices.getDownloadData(
                    UserAgentUtils.pickRandomUserAgent(), facebookUrl
                )
                if (!downloadResponse.isSuccessful) {
                    val error =
                        "${downloadResponse.code()} - " + downloadResponse.errorBody()?.string()
                    TrackerManager.logEvent(FacebookDownloadErrorEvent(error))
                    retryTimes--
                    delay(1000)
                    continue
                }
                html = downloadResponse.body()
                    ?.use { responseBody -> responseBody.use { res -> res.string() } } ?: ""

                if (html.isNotEmpty()) {
                    break
                }

                retryTimes--
                delay(1000)
            } while (retryTimes > 0)

            if (html.isEmpty()) {
                return DownloadContent()
            }

            val videoUrls = parseFacebookVideoLinks(html)

            val videos = videoUrls.map { pair ->
                DownloadData(
                    "${videoId}_${pair.first}", "video/mp4", pair.first, pair.second
                )
            }

            return DownloadContent(videos, emptyList(), emptyList())
        } catch (e: Exception) {
            TrackerManager.logEvent(FacebookDownloadErrorEvent(e.message))
            DownloadContent()
        }
    }

    private fun parseFacebookVideoLinks(htmlString: String): List<Pair<String, String>> {
        val jsoup = Jsoup.parse(htmlString)
        val aTags = jsoup.getElementsByTag("a")
        return aTags.filter { element -> element.hasAttr("href") && element.hasAttr("id") }
            .mapNotNull { element ->
                val href = element.attr("href") ?: ""
                val isVideoSD = element.id().contains(Regex("sdlink"))
                val isVideoHD = element.id().contains(Regex("hdlink"))
                if ((isVideoSD || isVideoHD) && href.isNotEmpty()) {
                    String.format(
                        Locale.getDefault(),
                        "Video %s",
                        if (isVideoSD) "SD" else "HD"
                    ) to href
                } else {
                    null
                }
            }
    }
}