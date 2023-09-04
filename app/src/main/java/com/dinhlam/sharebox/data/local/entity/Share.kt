package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.model.ShareData

@Entity(indices = [Index(value = ["share_id"], unique = true), Index(value = ["share_user_id"])])
data class Share(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("share_id") val shareId: String,
    @ColumnInfo("share_user_id") val shareUserId: String,
    @ColumnInfo("share_data") val shareData: ShareData,
    @ColumnInfo("is_video_share") val isVideoShare: Boolean,
    @ColumnInfo("share_note") val shareNote: String?,
    @ColumnInfo("share_box_id") val shareBoxId: String?,
    @ColumnInfo("share_date") val shareDate: Long,
    @ColumnInfo(name = "synced", defaultValue = "0") val synced: Boolean,
    @ColumnInfo("created_at") val createdAt: Long = nowUTCTimeInMillis(),
    @ColumnInfo("updated_at") val updatedAt: Long = nowUTCTimeInMillis()
)
