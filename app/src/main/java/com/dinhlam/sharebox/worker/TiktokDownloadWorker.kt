package com.dinhlam.sharebox.worker

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jsoup.Jsoup
import java.net.URLEncoder
import kotlin.random.Random

@HiltWorker
class TiktokDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
    private val sssTikServices: SSSTikServices,
    private val router: Router,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(createForegroundInfo())
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.success()
            val tiktokUrl = videoHelper.getTiktokFullUrl(sourceUrl)
            val videoId =
                Uri.parse(tiktokUrl).lastPathSegment ?: return@withContext Result.success()
            var retryTimes = 3
            var html = ""
            do {
                val encodeUrl = URLEncoder.encode(tiktokUrl, "utf-8")
                val sssTikId = "$encodeUrl&locale=en&tt=azhwU005"
                val requestBody = RequestBody.create(MediaType.parse("text/plain"), "id=$sssTikId")
                val sssTikResponse = sssTikServices.getDownloadLink(
                    UserAgentUtils.pickRandomUserAgent(), requestBody
                )
                if (!sssTikResponse.isSuccessful) {
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
                return@withContext if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
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

            val intent = router.downloadPopup(appContext, videos, audios, images)
            appContext.startActivity(intent)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
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

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            workerParams.inputData.getInt("id", Random.nextInt()),
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.download_preparing))
                .setAutoCancel(false)
                .setContentTitle(appContext.getString(R.string.downloading))
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(
                    0,
                    appContext.getString(R.string.cancel),
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}