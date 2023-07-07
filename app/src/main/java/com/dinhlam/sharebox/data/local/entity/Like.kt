package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis

@Entity(
    indices = [Index(
        value = ["share_id", "user_id"], unique = true
    ), Index(value = ["user_id"]), Index(value = ["like_id"], unique = true)]
)
data class Like(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("like_id") val likeId: String,
    @ColumnInfo("share_id") val shareId: String,
    @ColumnInfo("user_id") val userId: String,
    @ColumnInfo("like_date") val likeDate: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long = nowUTCTimeInMillis(),
)
