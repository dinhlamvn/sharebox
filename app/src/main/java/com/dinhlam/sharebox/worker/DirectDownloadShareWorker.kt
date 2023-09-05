package com.dinhlam.sharebox.worker

import android.content.Context
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
import kotlin.random.Random

@HiltWorker
class DirectDownloadShareWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        val shareUrl =
            workerParams.inputData.getString(AppExtras.EXTRA_URL) ?: return Result.success()
        val videoSource = videoHelper.getVideoSource(shareUrl) ?: return Result.success()
        val videoOriginUrl = videoHelper.getVideoOriginUrl(videoSource, shareUrl)
        videoHelper.downloadVideo(appContext, Random.nextInt(), videoSource, videoOriginUrl)
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            111222,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.downloading))
                .setContentTitle(appContext.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}