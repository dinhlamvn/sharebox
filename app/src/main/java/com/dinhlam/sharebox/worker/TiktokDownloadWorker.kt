package com.dinhlam.sharebox.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.pushNotification
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
import okhttp3.MultipartBody
import org.jsoup.Jsoup
import kotlin.random.Random

@HiltWorker
class TiktokDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
    private val sssTikServices: SSSTikServices,
    private val router: Router,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(createForegroundInfo())
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.success()
            val tiktokUrl = videoHelper.getTiktokUrl(sourceUrl)
            val videoId =
                Uri.parse(tiktokUrl).lastPathSegment ?: return@withContext Result.success()
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

            val intent = router.downloadPopup(appContext, tiktokUrl, videos, audios, images, notificationId)
            val notification = createDownloadNotification(intent, sourceUrl)
            appContext.pushNotification(notificationId, notification)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createDownloadNotification(intent: Intent, sourceUrl: String): Notification {
        return NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
            .setContentTitle(appContext.getString(R.string.completed))
            .setContentText(appContext.getString(R.string.download_ready, sourceUrl))
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(
                NotificationCompat.Action(
                    null,
                    appContext.getString(R.string.download),
                    PendingIntent.getActivity(appContext, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)
                )
            )
            .setAutoCancel(true)
            .build()
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                workerParams.inputData.getInt("id", notificationId),
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.download_preparing, workerParams.inputData.getString("url")))
                    .setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(
                        0,
                        appContext.getString(R.string.cancel),
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                workerParams.inputData.getInt("id", notificationId),
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.download_preparing, workerParams.inputData.getString("url")))
                    .setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(
                        0,
                        appContext.getString(R.string.cancel),
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build()
            )
        }
    }
}