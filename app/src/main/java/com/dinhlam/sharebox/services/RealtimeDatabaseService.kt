package com.dinhlam.sharebox.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareType
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeBoxObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeCommentObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeLikeObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeShareObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeUserObj
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.NetworkHelper
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RealtimeDatabaseService : Service() {

    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopSelf()
        }
    }

    companion object {
        private const val SERVICE_ID = 69919090
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
    lateinit var likeRepository: LikeRepository

    @Inject
    lateinit var boxRepository: BoxRepository

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var firebaseStorageHelper: FirebaseStorageHelper

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var videoHelper: VideoHelper

    @Inject
    lateinit var videoMixerRepository: VideoMixerRepository

    private var shouldRemoveOnTaskRemoved = false

    override fun onCreate() {
        super.onCreate()
        Logger.debug("$this is created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("$this is start command")
        registerReceiver(stopServiceReceiver, IntentFilter("$packageName.STOP_FOREGROUND_SERVICE"))


        shouldRemoveOnTaskRemoved =
            intent?.getBooleanExtra(AppExtras.EXTRA_SERVICE_STOP_FOR_TASK_REMOVED, false) ?: true

        startForeground(
            SERVICE_ID,
            NotificationCompat.Builder(this, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID).addAction(
                0, getString(R.string.remove), PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent("$packageName.STOP_FOREGROUND_SERVICE"),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setContentText(getString(R.string.realtime_database_service_noti_content))
                .setSubText(getString(R.string.realtime_database_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        packageManager.getLaunchIntentForPackage(packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        )

        realtimeDatabaseRepository.consumeShares(::onShareAdded)
        realtimeDatabaseRepository.consumeUsers(::onUserAdded)
        realtimeDatabaseRepository.consumeComments(::onCommentAdded)
        realtimeDatabaseRepository.consumeLikes(::onLikeAdded)
        realtimeDatabaseRepository.consumeBoxes(::onBoxAdded)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Logger.debug("RealtimeService is on task removed $shouldRemoveOnTaskRemoved")
        if (shouldRemoveOnTaskRemoved) {
            stopSelf()
        }
    }

    private fun onBoxAdded(boxId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val box = boxRepository.findOneRaw(boxId) ?: RealtimeBoxObj.from(jsonMap).run {
                boxRepository.insert(id, name, desc, createdBy, createdDate, passcode)
            }

            if (box == null) {
                Logger.error("Insert box from realtime-db to local failed")
            }
        }
    }

    private fun onShareAdded(shareId: String, jsonMap: Map<String, Any>) {
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

                val newShareData = shareData.cast<ShareData.ShareImage>()?.let { shareImage ->
                    firebaseStorageHelper.runCatching {
                        getImageDownloadUri(
                            shareId, shareImage.uri
                        )
                    }.getOrNull()?.let { downloadUri ->
                        shareImage.copy(uri = downloadUri)
                    }
                } ?: shareData.cast<ShareData.ShareImages>()?.let { shareImages ->
                    val downloadUris = shareImages.uris.asFlow().mapNotNull { uri ->
                        firebaseStorageHelper.runCatching {
                            getImageDownloadUri(
                                shareId, uri
                            )
                        }.getOrNull()
                    }.toList()
                    shareImages.copy(uris = downloadUris)
                } ?: shareData

                val insertedShare = shareRepository.insert(
                    shareId,
                    newShareData,
                    realtimeShareObj.shareNote,
                    realtimeShareObj.shareBoxId,
                    realtimeShareObj.shareUserId,
                    realtimeShareObj.shareDate
                )

                newShareData.cast<ShareData.ShareUrl>()?.let { shareDataUrl ->
                    val videoMixerDetail = videoMixerRepository.findOne(shareId)
                    val shareUrl = shareDataUrl.url
                    val videoSource =
                        videoMixerDetail?.source ?: videoHelper.getVideoSource(shareUrl)

                    if (videoSource is VideoSource.Tiktok) {
                        if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
                            return@let
                        }

                        if (!networkHelper.isNetworkConnected()) {
                            return@let
                        }
                    }

                    val videoSourceId =
                        videoMixerDetail?.sourceId ?: videoHelper.getVideoSourceId(
                            videoSource, shareUrl
                        )

                    val videoUri = videoMixerDetail?.uri?.takeIfNotNullOrBlank()
                        ?: videoHelper.getVideoUri(
                            this@RealtimeDatabaseService, videoSource, shareUrl
                        )

                    if (videoSource == VideoSource.Tiktok && videoUri == null) {
                        return@let
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
                }
            }
        }
    }

    private fun onUserAdded(userId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val realtimeUserObj = RealtimeUserObj.from(jsonMap)
            val user = userRepository.findOneRaw(userId) ?: User(
                userId = realtimeUserObj.userId,
                name = realtimeUserObj.name,
                avatar = realtimeUserObj.avatar,
                joinDate = realtimeUserObj.joinDate
            )

            val newUser = user.copy(
                name = realtimeUserObj.name,
                avatar = realtimeUserObj.avatar,
                level = realtimeUserObj.level,
                drama = realtimeUserObj.drama,
                joinDate = realtimeUserObj.joinDate
            )

            if (!userRepository.upsert(newUser)) {
                Logger.error("Upsert new user to database failed.")
            }
        }
    }

    private fun onCommentAdded(commentId: String, jsonMap: Map<String, Any>) {
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

    private fun onLikeAdded(likeId: String, jsonMap: Map<String, Any>) {
        serviceScope.launch {
            val realtimeLikeObj = RealtimeLikeObj.from(jsonMap)
            likeRepository.findOneRaw(likeId) ?: run {
                val like = likeRepository.like(
                    realtimeLikeObj.shareId,
                    realtimeLikeObj.userId,
                    realtimeLikeObj.likeId,
                    realtimeLikeObj.likeDate
                )

                if (like == null) {
                    Logger.error("Insert like from cloud to database failed.")
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
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}