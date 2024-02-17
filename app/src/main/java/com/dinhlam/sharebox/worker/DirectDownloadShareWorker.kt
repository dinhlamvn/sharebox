package com.dinhlam.sharebox.worker

import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.helper.VideoHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class DirectDownloadShareWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        try {
            val shareUrl =
                workerParams.inputData.getString(AppExtras.EXTRA_URL) ?: error("No share url")
            val videoSource = videoHelper.getVideoSource(shareUrl) ?: error("No video source")
            val videoOriginUrl =
                videoHelper.getVideoOriginUrl(videoSource, shareUrl) ?: error("No video url")
            videoHelper.downloadVideo(appContext, notificationId, videoSource, videoOriginUrl)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, R.string.nothing_to_download, Toast.LENGTH_SHORT).show()
            }
        }
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.download_preparing))
                .setAutoCancel(false).setContentTitle(appContext.getString(R.string.downloading))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}