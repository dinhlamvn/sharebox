package com.dinhlam.sharebox.worker

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanUpDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val videoMixerRepository: VideoMixerRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1231,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.cleanup_data_service_noti_content))
                .setSubText(appContext.getString(R.string.cleanup_data_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).build()
        )
    }

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        cleanUpOldData()
        return Result.success()
    }

    private suspend fun cleanUpOldData() {
        val videoCreatedTimeToCleanUp = nowUTCTimeInMillis() - AppConsts.VIDEO_DATA_ALIVE_TIME

        while (true) {
            val oldVideos = videoMixerRepository.findVideoToCleanUp(videoCreatedTimeToCleanUp)
            val outOffsetVideos = videoMixerRepository.findVideoToCleanUp()

            if (oldVideos.isEmpty() && outOffsetVideos.isEmpty()) {
                return
            }

            oldVideos.plus(outOffsetVideos).forEach { video ->
                if (!videoMixerRepository.delete(video)) {
                    Logger.error("Error clean up share $video")
                } else {
                    video.uri?.takeIf { uri -> uri.startsWith("content://") }?.let { videoUri ->
                        appContext.contentResolver.delete(Uri.parse(videoUri), null, null)
                    }
                    Logger.debug("Success remove share $video")
                }
            }
        }
    }
}