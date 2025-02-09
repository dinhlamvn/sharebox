package com.dinhlam.sharebox.data.realtime

import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
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
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
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
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
) {

    private val realtimeDatabaseScope = CoroutineScope(
        Executors.newCachedThreadPool()
            .asCoroutineDispatcher() + CoroutineName("realtime-database-scope")
    )

    private val shareEventListener =
        SimpleRealtimeEventListener(realtimeDatabaseScope, ::onShareSnapshotDataReceived)

    private val boxEventListener =
        SimpleRealtimeEventListener(realtimeDatabaseScope, ::onBoxSnapshotDataReceived)

    private val shareRef: DatabaseReference by lazyOf(database.getReference("shares"))

    private val userRef: DatabaseReference by lazyOf(database.getReference("users"))

    private val commentRef: DatabaseReference by lazyOf(database.getReference("comments"))

    private val likeRef: DatabaseReference by lazyOf(database.getReference("likes"))

    private val boxRef: DatabaseReference by lazyOf(database.getReference("boxes"))

    private val boxMemberRef: DatabaseReference by lazyOf(
        database.getReference("box-members").child(userHelper.getCurrentUserId())
    )
    private val boxMemberInvitedRef: DatabaseReference by lazyOf(database.getReference("box-members-invited"))

    suspend fun <T> push(record: T) {
        if (!userHelper.isSignedIn()) {
            return
        }
        when (record) {
            is Share -> pushShare(record)
            is User -> pushUser(record)
            is Comment -> pushComment(record)
            is Like -> pushLike(record)
            is Box -> pushBox(record)
        }
    }

    private suspend fun pushShare(share: Share) {
        try {
            shareRef.child(share.shareId).setValue(RealtimeShareObj.from(gson, share)).await()
            shareRepository.update(share.copy(synced = true), false)
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    private suspend fun pushUser(user: User) {
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

    private suspend fun pushComment(comment: Comment) {
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

    private suspend fun pushLike(like: Like) {
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

    private suspend fun pushBox(box: Box) {
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
        if (!userHelper.isSignedIn()) {
            return
        }
        shareRef.addValueEventListener(shareEventListener)
        boxRef.addValueEventListener(boxEventListener)
    }

    fun release() {
        shareRef.removeEventListener(shareEventListener)
        boxRef.removeEventListener(boxEventListener)
    }

    private fun createNewBox(boxId: String, jsonMap: Map<String, Any>): Box {
        return RealtimeBoxObj.from(jsonMap).run {
            Box(
                boxId = boxId,
                boxName = name,
                boxDesc = desc,
                createdBy = createdBy,
                createdDate = createdDate,
                passcode = passcode,
                lastSeen = System.currentTimeMillis(),
                synced = true
            )
        }
    }

    private suspend fun onBoxSnapshotDataReceived(boxId: String, jsonMap: Map<String, Any>) {
        try {
            val box = boxRepository.findOneRaw(boxId)
            val snapshotDataBox = createNewBox(boxId, jsonMap)
            if (box != null) {
                boxRepository.update(snapshotDataBox.copy(id = box.id, synced = true))
            } else {
                boxRepository.insert(snapshotDataBox.copy(synced = true))
            }
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    private fun createNewShare(shareId: String, jsonMap: Map<String, Any>): Share? {
        val realtimeShareObj = RealtimeShareObj.from(jsonMap)
        val json = gson.fromJson(realtimeShareObj.shareData, JsonObject::class.java)
        val shareData =
            when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
                ShareType.URL -> gson.fromJson(json, ShareData.ShareUrl::class.java)
                ShareType.TEXT -> gson.fromJson(json, ShareData.ShareText::class.java)
                ShareType.IMAGE -> gson.fromJson(json, ShareData.ShareImage::class.java)
                ShareType.IMAGES -> gson.fromJson(json, ShareData.ShareImages::class.java)
                else -> return null
            }
        return Share(
            shareId = shareId,
            shareUserId = realtimeShareObj.shareUserId,
            shareData = shareData,
            shareNote = realtimeShareObj.shareNote,
            shareBoxId = realtimeShareObj.shareBoxId,
            shareDate = realtimeShareObj.shareDate,
            synced = true,
            isVideoShare = realtimeShareObj.isVideoShare
        )
    }

    private suspend fun onShareSnapshotDataReceived(shareId: String, jsonMap: Map<String, Any>) {
        try {
            val share = shareRepository.findOneRaw(shareId)
            val snapshotDataShare = createNewShare(shareId, jsonMap) ?: return
            if (share != null) {
                shareRepository.update(snapshotDataShare.copy(id = share.id), false)
            } else {
                shareRepository.insert(snapshotDataShare.copy(synced = true))
            }
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    private class SimpleRealtimeEventListener(
        private val scope: CoroutineScope,
        private val block: suspend (String, Map<String, Any>) -> Unit
    ) : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            scope.launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
                try {
                    val iterator = snapshot.children.iterator()
                    while (iterator.hasNext()) {
                        val dataSnapshot = iterator.next()
                        val dataKey = dataSnapshot.key ?: continue
                        val value = dataSnapshot.value.cast<Map<String, Any>>() ?: continue
                        block(dataKey, value)
                    }
                } catch (e: Exception) {
                    Logger.error("$this Listen data change has error: $e")
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Logger.error("$this Listen data change has error: $error")
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