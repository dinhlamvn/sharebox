package com.dinhlam.sharebox.worker

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
            while (!realtimeDatabaseRepository.isDone() && currentTime < 5 * 60 * 1000) {
                delay(600)
                currentTime += 600
            }
            realtimeDatabaseRepository.cancel()
            startMixVideoFromShareData()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
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
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).setProgress(100, 0, true).build()
        )
    }

    private suspend fun startMixVideoFromShareData() {
        var currentOffset = 0
        while (true) {
            val shares = shareRepository.findForVideoMixer(
                20, currentOffset * 20
            )

            if (shares.isEmpty()) {
                return
            }

            val ids = mutableListOf<String>()

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
                        return@runCatching
                    }

                    val shareUrl = value.url
                    val videoSource =
                        videoMixerDetail?.source ?: videoHelper.getVideoSource(shareUrl) ?: return

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

                    val result = videoMixerRepository.upsert(
                        videoMixerDetail?.id.orElse(0),
                        shareId,
                        shareUrl,
                        videoSource,
                        videoSourceId,
                        videoUri?.toString(),
                        shareHelper.calcTrendingScore(shareId)
                    )

                    if (result) {
                        ids.add(shareId)
                    }
                }.onFailure { error ->
                    Logger.error("Had error while sync video content for share ${pair.key} - $error")
                }
            }
            Logger.debug("Video mixer success sync $ids - offset $currentOffset")
            currentOffset++
        }
    }
}