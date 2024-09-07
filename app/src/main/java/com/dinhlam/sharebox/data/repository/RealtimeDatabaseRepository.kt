package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.BoxMember
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.model.realtimedb.RealtimeBoxMemberObj
import com.dinhlam.sharebox.model.realtimedb.RealtimeBoxObj
import com.dinhlam.sharebox.model.realtimedb.RealtimeCommentObj
import com.dinhlam.sharebox.model.realtimedb.RealtimeLikeObj
import com.dinhlam.sharebox.model.realtimedb.RealtimeShareObj
import com.dinhlam.sharebox.model.realtimedb.RealtimeUserObj
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDatabaseRepository @Inject constructor(
    database: FirebaseDatabase,
    private val shareRepository: ShareRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val gson: Gson,
    private val firebaseStorageHelper: FirebaseStorageHelper,
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
) {

    private val realtimeDatabaseScope = CoroutineScope(
        Executors.newCachedThreadPool()
            .asCoroutineDispatcher() + CoroutineName("realtime-database-scope")
    )

    private val shareRef: DatabaseReference by lazyOf(database.getReference("shares"))

    private val userRef: DatabaseReference by lazyOf(database.getReference("users"))

    private val commentRef: DatabaseReference by lazyOf(database.getReference("comments"))

    private val likeRef: DatabaseReference by lazyOf(database.getReference("likes"))

    private val boxRef: DatabaseReference by lazyOf(database.getReference("boxes"))

    private val boxMemberRef: DatabaseReference by lazyOf(
        database.getReference("box-members").child(userHelper.getCurrentUserId())
    )
    private val boxMemberInvitedRef: DatabaseReference by lazyOf(database.getReference("box-members-invited"))

    suspend fun push(share: Share) {
        if (!userHelper.isSignedIn()) {
            return
        }
        try {
            shareRef.child(share.shareId).setValue(RealtimeShareObj.from(gson, share)).await()
            shareRepository.update(share.copy(synced = true))
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(user: User) {
        if (!userHelper.isSignedIn()) {
            return
        }
        try {
            userRef.child(user.userId).setValue(RealtimeUserObj.from(user)).await()
            userRepository.update(user.copy(synced = true))
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(comment: Comment) {
        if (!userHelper.isSignedIn()) {
            return
        }
        try {
            commentRef.child(comment.commentId).setValue(RealtimeCommentObj.from(comment)).await()
            commentRepository.update(comment.copy(synced = true))
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(like: Like) {
        if (!userHelper.isSignedIn()) {
            return
        }
        try {
            likeRef.child(like.likeId).setValue(RealtimeLikeObj.from(like)).await()
            likeRepository.update(like.copy(synced = true))
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    suspend fun push(box: Box) {
        if (!userHelper.isSignedIn()) {
            return
        }
        try {
            boxRef.child(box.boxId).setValue(RealtimeBoxObj.from(box)).await()
            boxRepository.update(box.copy(synced = true))
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    fun sync() {
        realtimeDatabaseScope.launch(Dispatchers.IO) {
            if (!userHelper.isSignedIn()) {
                return@launch
            }
            val shareListener = SimpleRealtimeEventListener(realtimeDatabaseScope, ::onShareAdded)
            val boxListener = SimpleRealtimeEventListener(realtimeDatabaseScope, ::onBoxAdded)
            shareRef.addListenerForSingleValueEvent(shareListener)
            boxRef.addListenerForSingleValueEvent(boxListener)
        }
    }

    private suspend fun onBoxAdded(boxId: String, jsonMap: Map<String, Any>) {
        val box = boxRepository.findOneRaw(boxId) ?: RealtimeBoxObj.from(jsonMap).run {
            boxRepository.insert(id, name, desc, createdBy, createdDate, passcode, synced = true)
        }

        if (box == null) {
            Logger.error("Insert box from realtime-db to local failed")
        }
    }

    private suspend fun onShareAdded(shareId: String, jsonMap: Map<String, Any>) = runCatching {
        shareRepository.findOneRaw(shareId) ?: run {
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
                realtimeShareObj.shareDate,
                synced = true,
                isVideoShare = realtimeShareObj.isVideoShare
            )
        }
    }

    private class SimpleRealtimeEventListener(
        private val scope: CoroutineScope,
        private val block: suspend (String, Map<String, Any>) -> Unit
    ) : ValueEventListener {
        var completed: Boolean = false

        override fun onDataChange(snapshot: DataSnapshot) {
            scope.launch {
                val iterator = snapshot.children.iterator()
                while (iterator.hasNext()) {
                    val dataSnapshot = iterator.next()
                    val dataKey = dataSnapshot.key ?: continue
                    val value = dataSnapshot.value.cast<Map<String, Any>>() ?: continue
                    block.invoke(dataKey, value)
                }
            }.invokeOnCompletion {
                completed = true
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Logger.error("consume data share error")
            Logger.error(error.message)
            completed = true
        }
    }

    suspend fun pushBoxMember(
        boxId: String,
        memberId: String,
        memberEmail: String
    ) {
        val createdTime = nowUTCTimeInMillis()
        val realtimeBoxMember = RealtimeBoxMemberObj(memberId, memberEmail, createdTime)
        boxMemberRef.child(boxId).push().setValue(realtimeBoxMember).await()
        boxMemberInvitedRef.child(memberId).child(boxId).push()
            .setValue(ServerValue.TIMESTAMP)
    }

    suspend fun removeBoxMember(boxId: String, dataKey: String, memberId: String) {
        boxMemberRef.child(boxId).child(dataKey).removeValue().await()
        boxMemberInvitedRef.child(memberId).child(boxId).removeValue()
    }

    fun onBoxMembersChange(boxId: String, block: (List<BoxMember>) -> Unit): ValueEventListener {
        return boxMemberRef.child(boxId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val members = snapshot.children.mapNotNull { dataSnapshot ->
                    val key = dataSnapshot.key ?: return@mapNotNull null
                    val value = dataSnapshot.value.castNonNull<Map<String, Any>>()
                    BoxMember(key, value["member_id"].toString(), value["member_email"].toString())
                }
                block.invoke(members)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.error("box member error")
            }
        })
    }

    fun removeBoxMembersChangeEvent(boxId: String, listener: ValueEventListener) {
        boxMemberRef.child(boxId).removeEventListener(listener)
    }

    fun onBoxMemberInvitedChange(block: (List<String>) -> Unit): ValueEventListener {
        return boxMemberInvitedRef.child(userHelper.getCurrentUserId())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val boxIdList = snapshot.children.mapNotNull { dataSnapshot ->
                        dataSnapshot.key
                    }
                    block.invoke(boxIdList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.error("box member error")
                }
            })
    }

    fun removeBoxMembersInvitedChangeEvent(listener: ValueEventListener) {
        boxMemberInvitedRef.child(userHelper.getCurrentUserId()).removeEventListener(listener)
    }
}