package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class, parentColumns = ["user_id"], childColumns = ["share_user_id"]
    )], indices = [Index(value = ["share_id"], unique = true), Index(value = ["share_user_id"])]
)
data class Share(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "share_id") val shareId: String,
    @ColumnInfo(name = "share_user_id") val shareUserId: String,
    @ColumnInfo(name = "share_data") val shareData: ShareData,
    @ColumnInfo(name = "is_video_share") val isVideoShare: Boolean,
    @ColumnInfo(name = "share_note") val shareNote: String?,
    @ColumnInfo(name = "share_box") val shareBox: Box,
    @ColumnInfo(name = "share_date") val shareDate: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long = nowUTCTimeInMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = nowUTCTimeInMillis()
)
