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
import com.dinhlam.sharebox.data.network.FDownServices
import com.dinhlam.sharebox.extensions.pushNotification
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.Locale
import kotlin.random.Random

@HiltWorker
class FacebookDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
    private val fDownServices: FDownServices,
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
            val facebookUrl = videoHelper.getFacebookUrl(sourceUrl)
            val videoId =
                Uri.parse(facebookUrl).lastPathSegment ?: return@withContext Result.success()
            var retryTimes = 3
            var html = ""
            do {
                val downloadResponse = fDownServices.getDownloadData(
                    UserAgentUtils.pickRandomUserAgent(), facebookUrl
                )
                if (!downloadResponse.isSuccessful) {
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
                return@withContext if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

            val videoUrls = parseVideoLinks(html)

            val videos = videoUrls.map { pair ->
                DownloadData(
                    "${videoId}_${pair.first}", "video/mp4", pair.first, pair.second
                )
            }

            val intent =
                router.downloadPopup(appContext, videos, emptyList(), emptyList(), notificationId)
            val notification = createDownloadNotification(intent, sourceUrl)
            appContext.pushNotification(notificationId, notification)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createDownloadNotification(intent: Intent, sourceUrl: String): Notification {
        return NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
            .setContentTitle(appContext.getString(R.string.download))
            .setContentText(appContext.getString(R.string.download_ready, sourceUrl))
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(
                NotificationCompat.Action(
                    null,
                    appContext.getString(R.string.download),
                    PendingIntent.getActivity(
                        appContext,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
            .setAutoCancel(true)
            .build()
    }

    private fun parseVideoLinks(htmlString: String): List<Pair<String, String>> {
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

    private fun createForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                workerParams.inputData.getInt("id", notificationId),
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.download_preparing))
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
                    .setContentText(appContext.getString(R.string.download_preparing))
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