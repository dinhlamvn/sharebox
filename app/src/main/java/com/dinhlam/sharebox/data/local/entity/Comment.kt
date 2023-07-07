package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis

@Entity(
    indices = [
        Index(value = ["share_id"]),
        Index(value = ["user_id"])
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "comment_id") val commentId: String,
    @ColumnInfo(name = "share_id") val shareId: String,
    @ColumnInfo(name = "user_id") val shareUserId: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "comment_date") val commentDate: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long = nowUTCTimeInMillis()
)
