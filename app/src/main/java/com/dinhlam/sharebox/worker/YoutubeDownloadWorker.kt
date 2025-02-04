package com.dinhlam.sharebox.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.downloader.Downloader
import com.dinhlam.sharebox.extensions.pushNotification
import com.dinhlam.sharebox.router.Router
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Named
import kotlin.random.Random

@HiltWorker
class YoutubeDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    @Named("YoutubeDownloader") private val youtubeDownloader: Downloader,
    private val router: Router,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(
            appContext.getString(
                R.string.download_preparing,
                workerParams.inputData.getString("url")
            )
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(
                createForegroundInfo(
                    appContext.getString(R.string.download_preparing)
                )
            )
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.failure()
            val downloadContent = youtubeDownloader.download(sourceUrl)
            val intent =
                router.downloadPopup(
                    appContext,
                    sourceUrl,
                    downloadContent.videos,
                    downloadContent.audios,
                    emptyList(),
                    notificationId
                )
            val notification = createDownloadNotification(intent, sourceUrl)
            if (!appContext.pushNotification(notificationId, notification)) {
                error("Push notification to device failed")
            }
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
                    PendingIntent.getActivity(
                        appContext,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
            .build()
    }

    private fun createForegroundInfo(subText: String): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                workerParams.inputData.getInt("id", Random.nextInt()),
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(subText).setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher).addAction(
                        0,
                        appContext.getString(R.string.cancel),
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                workerParams.inputData.getInt("id", Random.nextInt()),
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(subText).setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher).addAction(
                        0,
                        appContext.getString(R.string.cancel),
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build()
            )
        }
    }
}