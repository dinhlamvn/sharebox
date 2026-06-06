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

    private fun parseShareFromRealtimeObj(shareId: String, jsonMap: Map<String, Any>): Share? {
        return try {
            val realtimeShareObj = RealtimeShareObj.from(jsonMap)
            val json = gson.fromJson(realtimeShareObj.shareData, JsonObject::class.java)
            val shareData = when (
                enumByNameIgnoreCase(
                    json.get("type").asString,
                    ShareType.UNKNOWN
                )
            ) {
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

            Share(
                shareId = shareId,
                shareUserId = realtimeShareObj.shareUserId,
                shareData = shareData,
                isVideoShare = realtimeShareObj.isVideoShare,
                shareNote = realtimeShareObj.shareNote,
                shareBoxId = realtimeShareObj.shareBoxId,
                shareDate = realtimeShareObj.shareDate,
                synced = true,
                tagId = realtimeShareObj.tagId
            )
        } catch (e: Exception) {
            Logger.error(e)
            null
        }
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
        block: (List<BoxInvitedData>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val listener = boxMemberRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    realtimeDatabaseScope.launch(Dispatchers.IO) {
                        try {
                            val currentUserId = userHelper.getCurrentUserId()
                            val iterator = snapshot.children.iterator()
                            val boxInvitedDataList = buildList {
                                while (iterator.hasNext()) {
                                    val data = iterator.next()
                                    val boxId = data.key ?: continue

                                    val dataValue = data.value?.cast<Map<String, Any>>() ?: continue
                                    val memberData = dataValue[currentUserId]?.cast<Map<String, Any>>()
                                        ?: continue

                                    val invitedBy = memberData["invited_by"]?.toString() ?: continue
                                    val addedAt =
                                        memberData["added_at"]?.toString()?.toLongOrNull() ?: continue

                                    val boxData = boxRef.child(boxId).get().await()
                                    val valueMap = boxData.value?.cast<Map<String, Any>>() ?: continue
                                    val boxName =
                                        valueMap["name"]?.toString()?.takeIfNotNullOrBlank()
                                            ?: continue

                                    add(BoxInvitedData(boxId, boxName, invitedBy, addedAt))
                                }
                            }

                            block(boxInvitedDataList)
                        } catch (e: Exception) {
                            Logger.error(e)
                            onError(e)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.error("Listen invited box members cancelled: ${error.message}")
                    onError(error.toException())
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
                ref.removeEventListener(listener)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }
}
