package com.dinhlam.sharebox.downloader

import android.net.Uri
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.TiktokDownloadErrorEvent
import com.dinhlam.sharebox.utils.UserAgentUtils
import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import org.jsoup.Jsoup
import javax.inject.Inject

class TiktokDownloader @Inject constructor(
    private val videoHelper: VideoHelper,
    private val sssTikServices: SSSTikServices
) : Downloader {
    override suspend fun download(downloadUrl: String): DownloadContent {
        val tiktokUrl = videoHelper.getTiktokUrl(downloadUrl)
        val videoId =
            Uri.parse(tiktokUrl).lastPathSegment ?: error("No video id")
        var retryTimes = 3
        var html = ""
        do {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", tiktokUrl)
                .addFormDataPart("locale", "en")
                .addFormDataPart("tt", "a1kxcWUy")
                .build()

            val sssTikResponse = sssTikServices.getDownloadLink(
                UserAgentUtils.pickRandomUserAgent(), requestBody
            )
            if (!sssTikResponse.isSuccessful) {
                val error = "${sssTikResponse.code()} - " + sssTikResponse.errorBody()?.string()
                TrackerManager.logEvent(TiktokDownloadErrorEvent(error))
                retryTimes--
                delay(1000)
                continue
            }
            html = sssTikResponse.body()
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

        val imageUrls = parseHtmlSSSTikGallery(html)
        val videoUrls = parseVideoLink(html)
        val audioUrls = parseAudioLink(html)

        val videos = videoUrls?.let { videoUrl ->
            listOf(
                DownloadData(
                    videoId, "video/mp4", "(HD)", videoUrl
                )
            )
        } ?: emptyList()
        val audios = audioUrls?.let { audioUrl ->
            listOf(
                DownloadData(
                    videoId, "audio/mp3", "(MP3)", audioUrl
                )
            )
        } ?: emptyList()
        val images =
            imageUrls.map { imageUrl -> DownloadData(videoId, "image/jpg", "(JPG)", imageUrl) }

        return DownloadContent(videos, audios, images)
    }

    private fun parseHtmlSSSTikGallery(htmlString: String): List<String> {
        val jsoup = Jsoup.parse(htmlString)
        val slideTags = jsoup.getElementsByClass("splide__slide")
        return slideTags.mapNotNull { element ->
            element.getElementsByTag("a").firstOrNull()?.attr("href")
        }
    }

    private fun parseVideoLink(htmlString: String): String? {
        val jsoup = Jsoup.parse(htmlString)
        val aTags = jsoup.getElementsByTag("a")
        return aTags.filter { element -> element.hasAttr("href") }.firstOrNull { element ->
            val href = element.attr("href") ?: ""
            val isMp4Download = element.text().contains("Without watermark", true)
            href.contains("tikcdn.io") && isMp4Download
        }?.attr("href").takeIfNotNullOrBlank()
    }

    private fun parseAudioLink(htmlString: String): String? {
        val jsoup = Jsoup.parse(htmlString)
        val aTags = jsoup.getElementsByTag("a")
        return aTags.filter { element -> element.hasAttr("href") }.firstOrNull { element ->
            val href = element.attr("href") ?: ""
            val isAudioLink = element.text().contains("mp3", true)
            href.contains("tikcdn.io") && isAudioLink
        }?.attr("href").takeIfNotNullOrBlank()
    }
}