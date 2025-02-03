package com.dinhlam.sharebox.ui.download

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.FDownServices
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.events.TiktokDownloadErrorEvent
import com.dinhlam.sharebox.tracking.events.YoutubeDownloadErrorEvent
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val videoHelper: VideoHelper,
    private val okHttpClient: OkHttpClient,
    private val sssTikServices: SSSTikServices,
    private val libreTubeServices: LibreTubeServices,
    private val fDownServices: FDownServices,
) : BaseViewModel<DownloadState>(DownloadState()) {

    fun download(downloadLink: String) {
        suspend {
            val originUrl = getOriginUrl(downloadLink)
            val videoSource = videoHelper.getVideoSource(originUrl) ?: error("No video source")
            val videoOriginUrl =
                videoHelper.getVideoOriginUrl(videoSource, originUrl) ?: error("No video url")
            downloadVideo(videoSource, videoOriginUrl)
        }.execute { asyncLoad ->
            copy(asyncLoadDownload = asyncLoad)
        }
    }

    private suspend fun getOriginUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.use { responseBody -> responseBody.header("Location")!! }
        }

        val url = body.use { responseBody -> responseBody.request.url.toString() }

        if (url.contains("login.php?next=")) {
            Uri.parse(url).getQueryParameter("next")!!
        } else {
            url
        }
    }

    private suspend fun downloadVideo(
        urlSource: VideoSource,
        downloadUrl: String
    ): DownloadState.DownloadContent {
        return when (urlSource) {
            VideoSource.Tiktok -> downloadTiktok(downloadUrl)
            VideoSource.Youtube -> downloadYoutube(downloadUrl)
            VideoSource.Facebook -> downloadFacebook(downloadUrl)
        }
    }

    private suspend fun downloadYoutube(sourceUrl: String): DownloadState.DownloadContent {
        val videoId = getYoutubeVideoId(sourceUrl) ?: return DownloadState.DownloadContent()

        var retry = 3
        var strResponse = ""
        while (retry > 0) {
            val responseBody =
                libreTubeServices.getDownloadLink(UserAgentUtils.pickRandomUserAgent(), videoId)
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
            return DownloadState.DownloadContent()
        }

        val json = JSONObject(strResponse)
        val videoStreams = json.getJSONArray("videoStreams") ?: JSONArray()
        val audioStreams = json.getJSONArray("audioStreams") ?: JSONArray()

        if (videoStreams.length() == 0 && audioStreams.length() == 0) {
            return DownloadState.DownloadContent()
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

        return DownloadState.DownloadContent(videos, audios, emptyList())
    }

    private fun getYoutubeVideoId(sourceUrl: String): String? {
        val uri = Uri.parse(sourceUrl)
        if (sourceUrl.contains("/shorts/")) {
            return uri.lastPathSegment
        }
        return uri.getQueryParameter("v")
    }

    private suspend fun downloadFacebook(sourceUrl: String): DownloadState.DownloadContent {
        val facebookUrl = videoHelper.getFacebookUrl(sourceUrl)
        val videoId =
            Uri.parse(facebookUrl).lastPathSegment ?: error("No video id")
        var retryTimes = 3
        var html = ""
        do {
            val downloadResponse = fDownServices.getDownloadData(
                UserAgentUtils.pickRandomUserAgent(), facebookUrl
            )
            if (!downloadResponse.isSuccessful) {
                val error = "${downloadResponse.code()} - " + downloadResponse.errorBody()?.string()
                TrackerManager.logEvent(YoutubeDownloadErrorEvent(error))
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
            return DownloadState.DownloadContent()
        }

        val videoUrls = parseFacebookVideoLinks(html)

        val videos = videoUrls.map { pair ->
            DownloadData(
                "${videoId}_${pair.first}", "video/mp4", pair.first, pair.second
            )
        }

        return DownloadState.DownloadContent(videos, emptyList(), emptyList())
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

    private suspend fun downloadTiktok(sourceUrl: String): DownloadState.DownloadContent {
        val tiktokUrl = videoHelper.getTiktokUrl(sourceUrl)
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
            return DownloadState.DownloadContent()
        }

        val imageUrls = parseHtmlSSSTikGallery(html)
        val videoUrl = parseVideoLink(html)
        val audioUrl = parseAudioLink(html)

        val videos = videoUrl?.let { downloadUrl ->
            listOf(
                DownloadData(
                    videoId, "video/mp4", "(HD)", downloadUrl
                )
            )
        } ?: emptyList()
        val audios = audioUrl?.let { downloadUrl ->
            listOf(
                DownloadData(
                    videoId, "audio/mp3", "(MP3)", downloadUrl
                )
            )
        } ?: emptyList()
        val images =
            imageUrls.map { imageUrl -> DownloadData(videoId, "image/jpg", "(JPG)", imageUrl) }

        return DownloadState.DownloadContent(videos, audios, images)
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