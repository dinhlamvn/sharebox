package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.model.ShareType
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeCommentObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeShareObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeUserObj
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.logger.Logger
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RealtimeDatabaseService : Service() {

    companion object {
        private const val LIMIT_NUMBER_SYNC = 20
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var gson: Gson

    override fun onCreate() {
        super.onCreate()
        Logger.debug("$this is created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("$this is start command")
        realtimeDatabaseRepository.consumeShares(::handleShareAdded)
        realtimeDatabaseRepository.consumeUsers(::handleUserAdded)
        realtimeDatabaseRepository.consumeComments(::handleCommentAdded)
        return START_STICKY
    }

    private fun handleShareAdded(shareId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            shareRepository.findOneRaw(shareId) ?: run {
                val realtimeShareObj = RealtimeShareObj.from(jsonMap)
                val json = gson.fromJson(realtimeShareObj.shareData, JsonObject::class.java)
                val shareData =
                    when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
                        ShareType.URL -> gson.fromJson(json, ShareData.ShareUrl::class.java)
                        ShareType.TEXT -> gson.fromJson(json, ShareData.ShareText::class.java)
                        ShareType.IMAGE -> gson.fromJson(json, ShareData.ShareImage::class.java)
                        ShareType.IMAGES -> gson.fromJson(json, ShareData.ShareImages::class.java)
                        else -> error("Error while parse json string $json to ShareData")
                    }
                shareRepository.insert(
                    shareId,
                    shareData,
                    realtimeShareObj.shareNote,
                    ShareMode.ShareModeCommunity,
                    realtimeShareObj.shareUserId,
                    realtimeShareObj.shareDate
                )
            }
        }
    }

    private fun handleUserAdded(userId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val realtimeUserObj = RealtimeUserObj.from(jsonMap)
            val user = userRepository.findOneRaw(userId) ?: User(
                userId = realtimeUserObj.userId,
                name = realtimeUserObj.name,
                avatar = realtimeUserObj.avatar,
                joinDate = realtimeUserObj.joinDate
            )

            val newUser = user.copy(level = realtimeUserObj.level, drama = realtimeUserObj.drama)

            if (!userRepository.upsert(newUser)) {
                Logger.error("Upsert new user to database failed.")
            }
        }
    }

    private fun handleCommentAdded(commentId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val realtimeCommentObj = RealtimeCommentObj.from(jsonMap)
            commentRepository.findOneRaw(commentId) ?: run {
                val commentEntity = commentRepository.insert(
                    commentId,
                    realtimeCommentObj.shareId,
                    realtimeCommentObj.userId,
                    realtimeCommentObj.content,
                    realtimeCommentObj.commentDate
                )

                if (commentEntity == null) {
                    Logger.error("Insert comment from cloud to database failed.")
                }
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.debug("$this has been stopped")
        realtimeDatabaseRepository.cancel()
    }
}