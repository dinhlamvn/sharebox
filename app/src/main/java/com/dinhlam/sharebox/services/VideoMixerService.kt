package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.filterValuesNotNull
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.NetworkHelper
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VideoMixerService : Service() {

    companion object {
        private const val TIME_DELAY_WHEN_EMPTY = 10_000L
        private const val LIMIT_ITEM_SYNC = 20
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val binder = LocalBinder()

    @Inject
    lateinit var videoHelper: VideoHelper

    @Inject
    lateinit var videoMixerRepository: VideoMixerRepository

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var shareHelper: ShareHelper

    inner class LocalBinder : Binder() {
        fun getService(): VideoMixerService = this@VideoMixerService
    }

    override fun onBind(intent: Intent?): IBinder {
        Logger.debug("VideoMixerService bind")
        startMixVideoFromShareData()
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Logger.debug("VideoMixerService created")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.debug("VideoMixerService unbind")
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    private fun startMixVideoFromShareData() {
        serviceScope.launch {
            var currentOffset = 0
            while (isActive) {
                val shares = shareRepository.findForVideoMixer(
                    LIMIT_ITEM_SYNC, currentOffset * LIMIT_ITEM_SYNC
                )

                if (shares.isEmpty() && currentOffset == 0) {
                    continue
                }

                if (shares.isEmpty()) {
                    Logger.debug("Video mixer reset sync in offset $currentOffset")
                    currentOffset = 0
                    delay(TIME_DELAY_WHEN_EMPTY)
                    continue
                }

                val ids = mutableListOf<String>()

                val takenMapData = shares.associate { shareDetail ->
                    shareDetail.shareId to shareDetail.shareData.cast<ShareData.ShareUrl>()
                }.filterValuesNotNull()

                takenMapData.forEach { pair ->
                    pair.runCatching {
                        val shareId = key
                        val videoMixerDetail = videoMixerRepository.findOne(shareId)
                        val shareUrl = value.url
                        val videoSource =
                            videoMixerDetail?.source ?: videoHelper.getVideoSource(shareUrl)

                        if (videoSource is VideoSource.Tiktok) {
                            if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
                                return@forEach
                            }

                            if (!networkHelper.isNetworkConnected()) {
                                return@forEach
                            }
                        }

                        val videoSourceId =
                            videoMixerDetail?.sourceId ?: videoHelper.getVideoSourceId(
                                videoSource, shareUrl
                            )

                        val videoUri = videoMixerDetail?.uri?.takeIfNotNullOrBlank()
                            ?: videoHelper.getVideoUri(
                                this@VideoMixerService, videoSource, shareUrl
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
}