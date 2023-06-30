package com.dinhlam.sharebox.services

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareCommunityRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanUpWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val communityRepository: ShareCommunityRepository,
    private val videoMixerRepository: VideoMixerRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1231,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.cleanup_data_service_noti_content))
                .setSubText(appContext.getString(R.string.cleanup_data_service_noti_subtext))
                .setSmallIcon(R.drawable.ic_launcher_foreground).build()
        )
    }

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        cleanUpOldData()
        return Result.success()
    }

    private suspend fun cleanUpOldData() {
        val timeToCleanUp = nowUTCTimeInMillis() - AppConsts.DATA_ALIVE_TIME

        while (true) {
            val shareCommunities = communityRepository.findShareToCleanUp(timeToCleanUp)
            val videos = videoMixerRepository.findVideoToCleanUp(timeToCleanUp)

            if (shareCommunities.isEmpty() && videos.isEmpty()) {
                return
            }

            shareCommunities.forEach { shareCommunity ->
                if (!communityRepository.delete(shareCommunity)) {
                    Logger.error("Error clean up share $shareCommunity")
                } else {
                    Logger.debug("Success remove share $shareCommunity")
                }
            }

            videos.forEach { video ->
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