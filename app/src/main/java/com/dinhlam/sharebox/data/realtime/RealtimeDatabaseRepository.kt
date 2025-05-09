package com.dinhlam.sharebox.data.realtime

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.BoxInvitedData
import com.dinhlam.sharebox.model.BoxMember
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.model.realtime.RealtimeBoxMemberObj
import com.dinhlam.sharebox.model.realtime.RealtimeBoxObj
import com.dinhlam.sharebox.model.realtime.RealtimeCommentObj
import com.dinhlam.sharebox.model.realtime.RealtimeLikeObj
import com.dinhlam.sharebox.model.realtime.RealtimeShareObj
import com.dinhlam.sharebox.model.realtime.RealtimeUserObj
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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
        SimpleRealtimeEventListener(
            "ShareListener",
            realtimeDatabaseScope,
            ::onShareSnapshotDataReceived
        )

    private val boxEventListener =
        SimpleRealtimeEventListener(
            "BoxListener",
            realtimeDatabaseScope,
            ::onBoxSnapshotDataReceived
        )

    private val shareRef: DatabaseReference by lazyOf(database.getReference("shares"))

    private val userRef: DatabaseReference by lazyOf(database.getReference("users"))

    private val commentRef: DatabaseReference by lazyOf(database.getReference("comments"))

    private val likeRef: DatabaseReference by lazyOf(database.getReference("likes"))

    private val boxRef: DatabaseReference by lazyOf(database.getReference("boxes"))

    private val boxMemberRef: DatabaseReference by lazyOf(database.getReference("box-members"))

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
            boxRepository.update(box.copy(synced = true), false)
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    fun listen() {
        shareRef.orderByChild("share_user_id")
            .equalTo(userHelper.getCurrentUserId())
            .addValueEventListener(shareEventListener)

        boxRef.orderByChild("created_by")
            .equalTo(userHelper.getCurrentUserId())
            .addValueEventListener(boxEventListener)
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
                boxRepository.update(
                    snapshotDataBox.copy(
                        id = box.id,
                        synced = true,
                        lastSeen = box.lastSeen,
                        createdAt = box.createdAt,
                        updatedAt = box.updatedAt
                    )
                )
            } else {
                boxRepository.insert(snapshotDataBox.copy(synced = true))
            }
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    private fun parseShareFromRealtimeObj(shareId: String, jsonMap: Map<String, Any>): Share? {
        val realtimeShareObj = RealtimeShareObj.from(jsonMap)
        val json = gson.fromJson(realtimeShareObj.shareData, JsonObject::class.java)
        val shareData =
            when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
                ShareType.URL -> gson.fromJson(json, ShareData.ShareUrl::class.java)
                ShareType.TEXT -> gson.fromJson(json, ShareData.ShareText::class.java)
                ShareType.IMAGE -> gson.fromJson(json, ShareData.ShareImage::class.java)
                ShareType.IMAGES -> gson.fromJson(json, ShareData.ShareImages::class.java)
                ShareType.FILE -> gson.fromJson(json, ShareData.ShareFile::class.java)
                ShareType.CHECK_LIST -> gson.fromJson(json, ShareData.ShareCheckList::class.java)
                ShareType.NOTIFICATION -> gson.fromJson(
                    json,
                    ShareData.ShareNotification::class.java
                )

                ShareType.UNKNOWN -> null
            } ?: return null
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
            val snapshotDataShare = parseShareFromRealtimeObj(shareId, jsonMap) ?: return
            if (share != null) {
                shareRepository.update(
                    snapshotDataShare.copy(
                        id = share.id,
                        synced = true,
                        createdAt = share.createdAt,
                        updatedAt = share.updatedAt
                    ), false
                )
            } else {
                shareRepository.insert(snapshotDataShare.copy(synced = true))
            }
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    private class SimpleRealtimeEventListener(
        private val name: String,
        private val scope: CoroutineScope,
        private val block: suspend (String, Map<String, Any>) -> Unit
    ) : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            scope.launch(Dispatchers.IO) {
                try {
                    val iterator = snapshot.children.iterator()
                    while (iterator.hasNext()) {
                        val dataSnapshot = iterator.next()
                        val dataKey = dataSnapshot.key ?: continue
                        val value = dataSnapshot.value.cast<Map<String, Any>>() ?: continue
                        block(dataKey, value)
                    }
                } catch (e: Exception) {
                    Logger.withTag(name).error("$this Listen data change has error: $e")
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Logger.withTag(name).error("$this Listen data change has error: $error")
        }
    }

    suspend fun pushBoxMember(
        boxId: String,
        memberId: String,
        memberEmail: String
    ) {
        val realtimeBoxMember = RealtimeBoxMemberObj(
            memberId,
            memberEmail,
            userHelper.getCurrentUserId(),
            nowUTCTimeInMillis()
        )
        boxMemberRef.child(boxId).child(memberId).setValue(realtimeBoxMember).await()
    }

    suspend fun removeBoxMember(boxId: String, memberId: String) {
        boxMemberRef.child(boxId).child(memberId).removeValue().await()
    }

    fun listenBoxMembersChangeEvent(
        lifecycleOwner: LifecycleOwner,
        boxId: String,
        block: (List<BoxMember>) -> Unit
    ) {
        val listener = boxMemberRef.child(boxId).addValueEventListener(object : ValueEventListener {
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

        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                boxMemberRef.child(boxId).removeEventListener(listener)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    fun listenBoxMembersInvitedChangeEvent(
        lifecycleOwner: LifecycleOwner,
        block: (List<BoxInvitedData>) -> Unit
    ) {
        val listener = boxMemberRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    realtimeDatabaseScope.launch(Dispatchers.IO) {
                        val iterator = snapshot.children.iterator()
                        val boxInvitedDataList = buildList {
                            while (iterator.hasNext()) {
                                val data = iterator.next()
                                val boxId = data.key ?: continue

                                val dataValue = data.value?.cast<Map<String, Any>>() ?: continue
                                val memberData =
                                    dataValue[userHelper.getCurrentUserId()]?.cast<Map<String, Any>>()
                                        ?: continue

                                val invitedBy = memberData["invited_by"]?.toString() ?: continue
                                val addedAt =
                                    memberData["added_at"]?.toString()?.toLongOrNull() ?: continue

                                val boxData = boxRef.orderByChild("id").equalTo(boxId).get().await()
                                val boxDataMap = boxData.value?.cast<Map<String, Any>>() ?: continue
                                val valueMap =
                                    boxDataMap[boxId]?.cast<Map<String, Any>>() ?: continue
                                val boxName =
                                    valueMap["name"]?.toString()?.takeIfNotNullOrBlank()
                                        ?: continue

                                add(BoxInvitedData(boxId, boxName, invitedBy, addedAt))
                            }
                        }

                        block(boxInvitedDataList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.error("box member error")
                }
            })

        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                boxMemberRef.removeEventListener(listener)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    fun listenInvitedBoxShareListingChangeEvent(
        lifecycleOwner: LifecycleOwner,
        boxId: String,
        block: (List<Share>) -> Unit
    ) {
        val ref = shareRef.orderByChild("share_box_id")
            .equalTo(boxId)

        val listener = ref
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    realtimeDatabaseScope.launch(Dispatchers.IO) {
                        val iterator = snapshot.children.iterator()
                        val shares = buildList {
                            while (iterator.hasNext()) {
                                val dataSnapshot = iterator.next()
                                val shareId = dataSnapshot.key ?: continue
                                val jsonMap =
                                    dataSnapshot.value?.cast<Map<String, Any>>() ?: continue
                                parseShareFromRealtimeObj(shareId, jsonMap)?.let(::add)
                            }
                        }

                        block(shares)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.error(error.message)
                }
            })

        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                boxMemberRef.removeEventListener(listener)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }
}