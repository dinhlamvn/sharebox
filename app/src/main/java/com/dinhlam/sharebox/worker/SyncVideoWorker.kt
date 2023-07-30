package com.dinhlam.sharebox.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.ShareData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncVideoWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val shareRepository: ShareRepository,
    private val videoHelper: VideoHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        return try {
            setForeground(createForegroundInfo())
            val shares = shareRepository.findForSyncVideos(100)
            shares.forEach { share ->
                share.shareData.cast<ShareData.ShareUrl>()?.let { shareUrl ->
                    videoHelper.syncVideo(share.shareId, shareUrl.url)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.success()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            777888,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.sync_video_content_text))
                .setContentTitle(appContext.getString(R.string.sync_video_content_title))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false).setProgress(0, 0, true)
                .build()
        )
    }
}