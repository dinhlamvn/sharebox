package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.realtimedb.RealtimeDBShareObj
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
    private val database: FirebaseDatabase, private val gson: Gson
) {

    private var shareChildEventListener: SharesChildEventListener? = null

    private val shareRef: DatabaseReference by lazyOf(database.getReference("shares"))

    suspend fun push(share: Share) {
        try {
            shareRef.child(share.shareId).setValue(RealtimeDBShareObj.from(gson, share)).await()
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

    fun consumeDataShares(block: (String, RealtimeDBShareObj) -> Unit) {
        shareChildEventListener = SharesChildEventListener(block).also { listener ->
            shareRef.addChildEventListener(listener)
        }
    }

    fun cancelConsumeDataShares() {
        shareChildEventListener?.let { listener -> shareRef.removeEventListener(listener) }
    }

    private class SharesChildEventListener(private val block: (String, RealtimeDBShareObj) -> Unit) :
        ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val shareId = snapshot.key ?: return
            Logger.debug("share $shareId added")
            val value = snapshot.value.cast<Map<String, Any>>() ?: return
            block.invoke(shareId, RealtimeDBShareObj.from(value))
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val shareId = snapshot.key
            Logger.debug("share $shareId changed")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val shareId = snapshot.key
            Logger.debug("share $shareId removed")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            val shareId = snapshot.key
            Logger.debug("share $shareId moved - previous: $previousChildName")
        }

        override fun onCancelled(error: DatabaseError) {
            Logger.error("consume data share error")
            Logger.error(error.message)
        }
    }
}