package com.dinhlam.sharebox.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.filterValuesNotNull
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.NetworkHelper
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val shareRepository: ShareRepository,
    private val videoMixerRepository: VideoMixerRepository,
    private val appSettingHelper: AppSettingHelper,
    private val videoHelper: VideoHelper,
    private val networkHelper: NetworkHelper,
    private val shareHelper: ShareHelper,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val SERVICE_ID = 699190901
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        Logger.debug("$this has been started")
        setForeground(getForegroundInfo())
        return try {
            var currentTime = 0
            realtimeDatabaseRepository.consume()
            while (!realtimeDatabaseRepository.isDone() && currentTime < 60 * 1000) {
                delay(600)
                currentTime += 600
            }
            realtimeDatabaseRepository.cancel()
            startMixVideoFromShareData()
            notifyDataSyncSuccess()
            Result.success()
        } catch (e: Exception) {
            Result.success()
        } finally {
            realtimeDatabaseRepository.cancel()
        }
    }


    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.realtime_database_service_noti_content))
                .setContentTitle(appContext.getString(R.string.realtime_database_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setProgress(100, 0, true)
                .build()
        )
    }

    private fun notifyDataSyncSuccess() {
        val notification =
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.notify_data_sync_success_text))
                .setContentTitle(appContext.getString(R.string.notify_data_sync_success_title))
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false)
                .setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        val notificationManager =
            appContext.getSystemServiceCompat<NotificationManager>(Context.NOTIFICATION_SERVICE)
        notificationManager.notify(123113, notification)
    }

    private suspend fun startMixVideoFromShareData() {
        val shares = shareRepository.findForVideoMixer(
            20, 0
        )

        if (shares.isEmpty()) {
            return
        }

        val takenMapData = shares.associate { shareDetail ->
            shareDetail.shareId to shareDetail.shareData.cast<ShareData.ShareUrl>()
        }.filterValuesNotNull()

        takenMapData.forEach { pair ->
            pair.runCatching {
                val shareId = key
                val videoMixerDetail = videoMixerRepository.findOne(shareId)

                val hasSync = videoMixerDetail?.let { vmd ->
                    (vmd.source == VideoSource.Tiktok && vmd.uri != null) || vmd.source != VideoSource.Tiktok
                } ?: false

                if (hasSync) {
                    return@forEach
                }

                val shareUrl = value.url
                val videoSource = videoMixerDetail?.source ?: videoHelper.getVideoSource(shareUrl)
                ?: return@forEach

                if (videoSource is VideoSource.Tiktok) {
                    if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
                        return@forEach
                    }

                    if (!networkHelper.isNetworkConnected()) {
                        return@forEach
                    }
                }

                val videoSourceId = videoMixerDetail?.sourceId ?: videoHelper.getVideoSourceId(
                    videoSource, shareUrl
                )

                val videoUri = videoHelper.getVideoUri(
                    appContext, videoSource, shareUrl
                )

                if (videoSource == VideoSource.Tiktok && videoUri == null) {
                    return@forEach
                }

                videoMixerRepository.upsert(
                    videoMixerDetail?.id.orElse(0),
                    shareId,
                    shareUrl,
                    videoSource,
                    videoSourceId,
                    videoUri?.toString(),
                    shareHelper.calcTrendingScore(shareId)
                )
            }.onFailure { error ->
                Logger.error("Had error while sync video content for share ${pair.key} - $error")
            }
        }
    }
}