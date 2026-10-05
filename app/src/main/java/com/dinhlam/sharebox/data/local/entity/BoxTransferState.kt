package com.dinhlam.sharebox.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local transfer history. Never replaced by remote account synchronization. */
@Entity(tableName = "box_transfer_state")
data class BoxTransferState(
    @PrimaryKey val boxId: String,
    val manifestJson: String,
    val localFingerprint: String,
    val isPublisher: Boolean,
)
