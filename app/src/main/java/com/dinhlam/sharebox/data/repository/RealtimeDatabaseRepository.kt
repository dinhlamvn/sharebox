package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeBoxObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeCommentObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeLikeObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeShareObj
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeUserObj
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.logger.Logger
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDatabaseRepository @Inject constructor(
    database: FirebaseDatabase, private val gson: Gson
) {

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

    fun consumeShares(childAddedHandler: (String, Map<String, Any>) -> Unit) {
        shareChildEventListener =
            SimpleRealtimeChildEventListener(childAddedHandler).also { listener ->
                shareRef.addChildEventListener(listener)
            }
    }

    fun consumeUsers(childAddedHandler: (String, Map<String, Any>) -> Unit) {
        userChildEventListener =
            SimpleRealtimeChildEventListener(childAddedHandler).also { listener ->
                userRef.addChildEventListener(listener)
            }
    }

    fun consumeComments(childAddedHandler: (String, Map<String, Any>) -> Unit) {
        commentChildEventListener =
            SimpleRealtimeChildEventListener(childAddedHandler).also { listener ->
                commentRef.addChildEventListener(listener)
            }
    }

    fun consumeLikes(childAddedHandler: (String, Map<String, Any>) -> Unit) {
        likeChildEventListener =
            SimpleRealtimeChildEventListener(childAddedHandler).also { listener ->
                likeRef.addChildEventListener(listener)
            }
    }

    fun consumeBoxes(childAddedHandler: (String, Map<String, Any>) -> Unit) {
        boxChildEventListener =
            SimpleRealtimeChildEventListener(childAddedHandler).also { listener ->
                boxRef.addChildEventListener(listener)
            }
    }

    fun cancel() {
        shareChildEventListener?.let { listener -> shareRef.removeEventListener(listener) }
        userChildEventListener?.let { listener -> userRef.removeEventListener(listener) }
        commentChildEventListener?.let { listener -> commentRef.removeEventListener(listener) }
        likeChildEventListener?.let { listener -> likeRef.removeEventListener(listener) }
        boxChildEventListener?.let { listener -> boxRef.removeEventListener(listener) }
    }

    private class SimpleRealtimeChildEventListener(private val block: (String, Map<String, Any>) -> Unit) :
        ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val dataKey = snapshot.key ?: return
            Logger.debug("Data with key $dataKey added")
            val value = snapshot.value.cast<Map<String, Any>>() ?: return
            block.invoke(dataKey, value)
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
        }
    }
}