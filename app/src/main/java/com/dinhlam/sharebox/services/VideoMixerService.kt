package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.filterValuesNotNull
import com.dinhlam.sharebox.extensions.orElse
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
        private const val TIME_DELAY = 60_000L
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
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var voteRepository: VoteRepository


    inner class LocalBinder : Binder() {
        fun getService(): VideoMixerService = this@VideoMixerService
    }

    override fun onBind(intent: Intent?): IBinder {
        Logger.debug("VideoMixerService bind")
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

    fun startMixVideoFromShareData() {
        serviceScope.launch {
            var currentOffset = 0
            while (isActive) {
                val shares = shareRepository.find(
                    ShareMode.ShareModeCommunity, LIMIT_ITEM_SYNC, currentOffset
                )

                if (shares.isEmpty()) {
                    Logger.debug("Reset sync in offset $currentOffset")
                    currentOffset = 0
                    delay(TIME_DELAY)
                    continue
                }

                val ids = mutableListOf<String>()

                val takenMapData = shares.associate { shareDetail ->
                    shareDetail.shareId to shareDetail.shareData.cast<ShareData.ShareUrl>()
                }.filterValuesNotNull()

                takenMapData.forEach { pair ->
                    val shareId = pair.key
                    val videoMixerDetail = videoMixerRepository.findOne(shareId)
                    val shareUrl = pair.value.url
                    val videoSource = videoHelper.getVideoSource(shareUrl)
                    val videoSourceId = videoHelper.getVideoSourceId(shareUrl)

                    val result = videoMixerRepository.upsert(
                        videoMixerDetail?.id.orElse(0),
                        shareId,
                        shareUrl,
                        videoSource,
                        videoSourceId,
                        null,
                        0
                    )

                    if (result) {
                        ids.add(shareId)
                    }
                }
                Logger.debug("Success sync $ids - offset $currentOffset")
                delay(TIME_DELAY)
                currentOffset += LIMIT_ITEM_SYNC
            }
        }
    }
}