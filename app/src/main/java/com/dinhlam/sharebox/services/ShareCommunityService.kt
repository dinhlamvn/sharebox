package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.ShareCommunityRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ShareCommunityService : Service() {

    companion object {
        private const val TIME_DELAY_WHEN_EMPTY = 60_000L
        private const val LIMIT_ITEM_SYNC = 20
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val binder = LocalBinder()

    @Inject
    lateinit var shareCommunityRepository: ShareCommunityRepository

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var voteRepository: VoteRepository


    inner class LocalBinder : Binder() {
        fun getService(): ShareCommunityService = this@ShareCommunityService
    }

    override fun onBind(intent: Intent?): IBinder {
        Logger.debug("ShareCommunityService bind")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Logger.debug("ShareCommunityService created")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.debug("ShareCommunityService unbind")
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    fun syncShareCommunityData() {
        serviceScope.launch {
            var currentOffset = 0
            while (isActive) {
                val shares = shareRepository.find(
                    ShareMode.ShareModeCommunity, LIMIT_ITEM_SYNC, currentOffset
                )

                if (shares.isEmpty()) {
                    Logger.debug("Reset sync in offset $currentOffset")
                    currentOffset = 0
                    delay(TIME_DELAY_WHEN_EMPTY)
                    continue
                }

                val ids = mutableListOf<String>()
                shares.forEach { shareDetail ->
                    val shareCommunity = shareCommunityRepository.findOne(shareDetail.shareId)
                    if (shareCommunityRepository.insert(
                            shareCommunity?.id.orElse(0),
                            shareDetail.shareId,
                            calcSharePower(shareDetail.shareId, shareDetail.createdAt)
                        )
                    ) {
                        ids.add(shareDetail.shareId)
                    }
                }
                Logger.debug("Success sync $ids - offset $currentOffset")
                currentOffset += LIMIT_ITEM_SYNC
            }
        }
    }

    private suspend fun calcSharePower(shareId: String, createdAt: Long): Int {
        var sharePower = 0

        val commentCount = commentRepository.count(shareId)
        sharePower += commentCount

        val voteCount = voteRepository.count(shareId)
        sharePower += voteCount

        val elapsed = Calendar.getInstance().timeInMillis - createdAt
        val hours = elapsed.div(3600 * 1000).toInt()

        return sharePower - hours
    }
}