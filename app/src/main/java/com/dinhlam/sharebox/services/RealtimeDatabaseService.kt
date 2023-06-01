package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.model.ShareType
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeDBShareObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeDBUserObj
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
    lateinit var gson: Gson

    override fun onCreate() {
        super.onCreate()
        Logger.debug("$this is created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("$this is start command")
        realtimeDatabaseRepository.consumeShares(::handleShareAdded)
        realtimeDatabaseRepository.consumeUsers(::handleUserAdded)
        return START_STICKY
    }

    private fun handleShareAdded(shareId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            shareRepository.findOneRaw(shareId) ?: return@launch run {
                val realtimeDBShareObj = RealtimeDBShareObj.from(jsonMap)
                val json = gson.fromJson(realtimeDBShareObj.shareData, JsonObject::class.java)
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
                    realtimeDBShareObj.shareNote,
                    ShareMode.ShareModeCommunity,
                    realtimeDBShareObj.shareUserId,
                    realtimeDBShareObj.shareDate
                )
            }
        }
    }

    private fun handleUserAdded(userId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val realtimeDBUserObj = RealtimeDBUserObj.from(jsonMap)
            val user = userRepository.findOneRaw(userId) ?: User(
                userId = realtimeDBUserObj.userId,
                name = realtimeDBUserObj.name,
                avatar = realtimeDBUserObj.avatar,
                joinDate = realtimeDBUserObj.joinDate
            )

            val newUser =
                user.copy(level = realtimeDBUserObj.level, drama = realtimeDBUserObj.drama)

            if (!userRepository.upsert(newUser)) {
                Logger.error("Upsert new user to database failed.")
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.debug("$this has been stopped")
        realtimeDatabaseRepository.cancelConsumeShares()
        realtimeDatabaseRepository.cancelConsumeUsers()
    }
}