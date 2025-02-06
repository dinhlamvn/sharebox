package com.dinhlam.sharebox.downloader

import android.net.Uri
import com.dinhlam.sharebox.data.network.AppDLServices
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadContent
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.TiktokDownloadErrorEvent
import org.jsoup.Jsoup
import javax.inject.Inject

class TiktokDownloaderV2 @Inject constructor(
    private val videoHelper: VideoHelper,
    private val appDLServices: AppDLServices
) : Downloader {
    override suspend fun download(downloadUrl: String): DownloadContent {
        return try {
            val tiktokUrl = videoHelper.getTiktokUrl(downloadUrl)
            val videoId =
                Uri.parse(tiktokUrl).lastPathSegment ?: error("No video id")
            val ts = ((System.currentTimeMillis() / 1000) / 60).toString()
            val s = "%s%s%s%s%s".format(ts, "1.136", tiktokUrl, "ssstik.io", "b0lF_14022023_DK")
            var str3 = ""
            for (ch in s.toCharArray()) {
                val sb = StringBuilder()
                sb.append(str3)
                sb.append("%03d".format(ch.code))
                str3 = sb.toString()
            }
            val str4 = "%d%s".format(str3.length, str3)
            val tt = str4.md5()
            val response = appDLServices.fetch(tiktokUrl, "en", tt, ts)!!
            val videos = buildList {
                if (response.noWatermarkLink.isNotEmpty()) {
                    add(
                        DownloadData(
                            videoId, "video/mp4", "(No Watermark)", response.noWatermarkLink
                        )
                    )
                }
                if (response.noWatermarkLinkHd.isNotEmpty()) {
                    add(
                        DownloadData(
                            videoId, "video/mp4", "(No Watermark - HD)", response.noWatermarkLinkHd
                        )
                    )
                }
            }
            val audios = listOf(
                DownloadData(
                    videoId, "audio/mp3", "(MP3)", response.musicLink
                )
            )
            val images = response.slides?.slideDataList.orEmpty().mapIndexed { index, slideData ->
                DownloadData(
                    "${videoId}_$index",
                    "image/jpg",
                    "(${slideData.width}x${slideData.height})",
                    slideData.url
                )
            }
            return DownloadContent(videos, audios, images)
        } catch (e: Exception) {
            TrackerManager.logEvent(TiktokDownloadErrorEvent(e.message))
            DownloadContent()
        }
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