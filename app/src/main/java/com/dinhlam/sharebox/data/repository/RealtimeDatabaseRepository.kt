package com.dinhlam.sharebox.data.repository

import android.content.Context
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
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
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.NetworkHelper
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.logger.Logger
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDatabaseRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    database: FirebaseDatabase,
    private val shareRepository: ShareRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val gson: Gson,
    private val firebaseStorageHelper: FirebaseStorageHelper,
    private val shareHelper: ShareHelper,
    private val appSettingHelper: AppSettingHelper,
    private val networkHelper: NetworkHelper,
    private val videoHelper: VideoHelper,
    private val videoMixerRepository: VideoMixerRepository,
    private val boxRepository: BoxRepository,
) {

    private val realtimeDatabaseScope = CoroutineScope(
        Executors.newCachedThreadPool()
            .asCoroutineDispatcher() + CoroutineName("realtime-database-scope")
    )

    private var shareChildEventListener: SimpleRealtimeChildEventListener? = null

    private var userChildEventListener: SimpleRealtimeChildEventListener? = null

    private var commentChildEventListener: SimpleRealtimeChildEventListener? = null

    private var likeChildEventListener: SimpleRealtimeChildEventListener? = null

    private var boxChildEventListener: SimpleRealtimeChildEventListener? = null

    private val shareRef: DatabaseReference by lazyOf(database.getReference("shares"))

    private val userRef: DatabaseReference by lazyOf(database.getReference("users"))

    private val commentRef: DatabaseReference by lazyOf(database.getReference("comments"))

    private val likeRef: DatabaseReference by lazyOf(database.getReference("likes"))

    private val boxRef: DatabaseReference by lazyOf(database.getReference("boxes"))

    suspend fun push(share: Share) {
        try {
            shareRef.child(share.shareId).setValue(RealtimeShareObj.from(gson, share)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(user: User) {
        try {
            userRef.child(user.userId).setValue(RealtimeUserObj.from(user)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(comment: Comment) {
        try {
            commentRef.child(comment.commentId).setValue(RealtimeCommentObj.from(comment)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(like: Like) {
        try {
            likeRef.child(like.likeId).setValue(RealtimeLikeObj.from(like)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(box: Box) {
        try {
            boxRef.child(box.boxId).setValue(RealtimeBoxObj.from(box)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    fun consume() {
        consumeShares(::onShareAdded)
        consumeUsers(::onUserAdded)
        consumeComments(::onCommentAdded)
        consumeLikes(::onLikeAdded)
        consumeBoxes(::onBoxAdded)
    }

    suspend fun isDone(): Boolean {
        val isShareDone = shareChildEventListener?.isDone() ?: false
        val isUserDone = userChildEventListener?.isDone() ?: false
        val isCommentDone = commentChildEventListener?.isDone() ?: false
        val isLikeDone = likeChildEventListener?.isDone() ?: false
        val isBoxDone = boxChildEventListener?.isDone() ?: false
        return isShareDone && isUserDone && isCommentDone && isLikeDone && isBoxDone
    }

    private fun consumeShares(childAddedHandler: suspend (String, Map<String, Any>) -> Unit) {
        shareChildEventListener = SimpleRealtimeChildEventListener(
            realtimeDatabaseScope, childAddedHandler, shareRef
        ).also { listener ->
            shareRef.addChildEventListener(listener)
        }
    }

    private fun consumeUsers(childAddedHandler: suspend (String, Map<String, Any>) -> Unit) {
        userChildEventListener = SimpleRealtimeChildEventListener(
            realtimeDatabaseScope, childAddedHandler, userRef
        ).also { listener ->
            userRef.addChildEventListener(listener)
        }
    }

    private fun consumeComments(childAddedHandler: suspend (String, Map<String, Any>) -> Unit) {
        commentChildEventListener = SimpleRealtimeChildEventListener(
            realtimeDatabaseScope, childAddedHandler, commentRef
        ).also { listener ->
            commentRef.addChildEventListener(listener)
        }
    }

    private fun consumeLikes(childAddedHandler: suspend (String, Map<String, Any>) -> Unit) {
        likeChildEventListener = SimpleRealtimeChildEventListener(
            realtimeDatabaseScope, childAddedHandler, likeRef
        ).also { listener ->
            likeRef.addChildEventListener(listener)
        }
    }

    private fun consumeBoxes(childAddedHandler: suspend (String, Map<String, Any>) -> Unit) {
        boxChildEventListener = SimpleRealtimeChildEventListener(
            realtimeDatabaseScope, childAddedHandler, boxRef
        ).also { listener ->
            boxRef.addChildEventListener(listener)
        }
    }

    private suspend fun onBoxAdded(boxId: String, jsonMap: Map<String, Any>) {
        val box = boxRepository.findOneRaw(boxId) ?: RealtimeBoxObj.from(jsonMap).run {
            boxRepository.insert(id, name, desc, createdBy, createdDate, passcode)
        }

        if (box == null) {
            Logger.error("Insert box from realtime-db to local failed")
        }
    }

    private suspend fun onShareAdded(shareId: String, jsonMap: Map<String, Any>) = runCatching {
        val share = shareRepository.findOneRaw(shareId) ?: run {
            val realtimeShareObj = RealtimeShareObj.from(jsonMap)

            val json = gson.fromJson(realtimeShareObj.shareData, JsonObject::class.java)
            val shareData =
                when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
                    ShareType.URL -> gson.fromJson(json, ShareData.ShareUrl::class.java)
                    ShareType.TEXT -> gson.fromJson(json, ShareData.ShareText::class.java)
                    ShareType.IMAGE -> gson.fromJson(json, ShareData.ShareImage::class.java)
                    ShareType.IMAGES -> gson.fromJson(json, ShareData.ShareImages::class.java)
                    else -> return@run null
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

            shareRepository.insert(
                shareId,
                newShareData,
                realtimeShareObj.shareNote,
                realtimeShareObj.shareBoxId,
                realtimeShareObj.shareUserId,
                realtimeShareObj.shareDate
            )
        }

        share?.takeIf { it.isVideoShare && it.shareDate >= nowUTCTimeInMillis() - AppConsts.DATA_ALIVE_TIME }?.shareData.cast<ShareData.ShareUrl>()
            ?.let { shareDataUrl ->
                val videoMixerDetail = videoMixerRepository.findOne(shareId)
                val shareUrl = shareDataUrl.url
                val videoSource =
                    videoMixerDetail?.source ?: videoHelper.getVideoSource(shareUrl)
                    ?: return@runCatching

                if (videoSource is VideoSource.Tiktok) {
                    if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
                        return@let
                    }

                    if (!networkHelper.isNetworkConnected()) {
                        return@let
                    }
                }

                val videoSourceId = videoMixerDetail?.sourceId ?: videoHelper.getVideoSourceId(
                    videoSource, shareUrl
                )

                val videoUri =
                    videoMixerDetail?.uri?.takeIfNotNullOrBlank() ?: videoHelper.getVideoUri(
                        appContext, videoSource, shareUrl
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

    private suspend fun onUserAdded(userId: String, jsonMap: Map<String, Any>) {
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

    private suspend fun onCommentAdded(commentId: String, jsonMap: Map<String, Any>) {
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

    private suspend fun onLikeAdded(likeId: String, jsonMap: Map<String, Any>) {
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

    fun cancel() {
        shareChildEventListener?.let { listener -> shareRef.removeEventListener(listener) }
        userChildEventListener?.let { listener -> userRef.removeEventListener(listener) }
        commentChildEventListener?.let { listener -> commentRef.removeEventListener(listener) }
        likeChildEventListener?.let { listener -> likeRef.removeEventListener(listener) }
        boxChildEventListener?.let { listener -> boxRef.removeEventListener(listener) }
    }

    private class SimpleRealtimeChildEventListener(
        private val scope: CoroutineScope,
        private val block: suspend (String, Map<String, Any>) -> Unit,
        private val databaseReference: DatabaseReference
    ) : ChildEventListener {

        @Volatile
        private var currentIndex = 0

        private val childrenCount = suspend {
            databaseReference.get().await().childrenCount.toInt()
        }

        suspend fun isDone(): Boolean = withContext(Dispatchers.IO) {
            currentIndex < childrenCount.invoke()
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val dataKey = snapshot.key ?: return
            Logger.debug("Data with key $dataKey added")
            val value = snapshot.value.cast<Map<String, Any>>() ?: return
            scope.launch {
                block.invoke(dataKey, value)
                currentIndex++
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val dataKey = snapshot.key ?: return
            Logger.debug("Data with key $dataKey changed")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val dataKey = snapshot.key ?: return
            Logger.debug("Data with key $dataKey removed")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            val dataKey = snapshot.key ?: return
            Logger.debug("Data with key $dataKey moved - previous: $previousChildName")
        }

        override fun onCancelled(error: DatabaseError) {
            Logger.error("consume data share error")
            Logger.error(error.message)
            currentIndex = Int.MAX_VALUE
        }
    }
}