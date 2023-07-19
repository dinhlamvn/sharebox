package com.dinhlam.sharebox.worker

import android.content.Context
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.filterValuesNotNull
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncVideosWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val shareRepository: ShareRepository,
    private val videoHelper: VideoHelper,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val SERVICE_ID = 1011011
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0)
    }

    override suspend fun doWork(): Result {
        return try {
            setForeground(createForegroundInfo(0))
            //syncVideos()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createForegroundInfo(@IntRange(from = 0, to = 100) progress: Int): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.sync_videos_notification_content))
                .setContentTitle(appContext.getString(R.string.sync_videos_notification_title))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false)
                .setProgress(100, progress, false)
                .build()
        )
    }

    private suspend fun syncVideos() {
        val shares = shareRepository.findForSyncVideos(20, 0)

        if (shares.isEmpty()) {
            return
        }

        val takenMapData = shares.associate { shareDetail ->
            shareDetail.shareId to shareDetail.shareData.cast<ShareData.ShareUrl>()
        }.filterValuesNotNull().toList()

        takenMapData.forEachIndexed { index, pair ->
            pair.runCatching {
                val shareId = pair.first
                val shareUrl = pair.second.url
                videoHelper.syncVideo(shareId, shareUrl)
            }.onFailure { error ->
                Logger.error("Had error while sync video content for share ${pair.first} - $error")
            }
            setForeground(createForegroundInfo((index + 1) * 5))
        }

    }
}