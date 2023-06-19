package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareCommunityRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CleanUpDataService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var communityRepository: ShareCommunityRepository

    @Inject
    lateinit var videoMixerRepository: VideoMixerRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("Cleanup service is started")

        startForeground(
            1231,
            NotificationCompat.Builder(this, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
                .setContentText(getString(R.string.cleanup_data_service_noti_content))
                .setSubText(getString(R.string.cleanup_data_service_noti_subtext))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        )

        serviceScope.launch {
            cleanUpCommunityShareData()
            cleanUpVideoMixerData()
            ServiceCompat.stopForeground(
                this@CleanUpDataService, ServiceCompat.STOP_FOREGROUND_REMOVE
            )
        }

        return START_NOT_STICKY
    }

    private suspend fun cleanUpCommunityShareData() {
        val timeToCleanUp = nowUTCTimeInMillis() - AppConsts.DATA_ALIVE_TIME

        while (true) {
            val shareCommunities = communityRepository.findShareToCleanUp(timeToCleanUp)

            if (shareCommunities.isEmpty()) {
                return
            }

            shareCommunities.forEach { shareCommunity ->
                if (!communityRepository.delete(shareCommunity)) {
                    Logger.error("Error clean up share $shareCommunity")
                } else {
                    Logger.debug("Success remove share $shareCommunity")
                }
            }
        }
    }

    private suspend fun cleanUpVideoMixerData() {
        val timeToCleanUp = nowUTCTimeInMillis() - AppConsts.DATA_ALIVE_TIME

        while (true) {
            val videos = videoMixerRepository.findVideoToCleanUp(timeToCleanUp)

            if (videos.isEmpty()) {
                return
            }

            videos.forEach { video ->
                if (!videoMixerRepository.delete(video)) {
                    Logger.error("Error clean up share $video")
                } else {
                    video.uri?.takeIf { uri -> uri.startsWith("content://") }?.let { videoUri ->
                        contentResolver.delete(Uri.parse(videoUri), null, null)
                    }
                    Logger.debug("Success remove share $video")
                }
            }
        }
    }
}