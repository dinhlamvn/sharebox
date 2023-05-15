package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Share::class,
            parentColumns = ["share_id"],
            childColumns = ["share_id"]
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"]
        )
    ],
    indices = [
        Index(value = ["share_id"]),
        Index(value = ["user_id"])
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "share_id") val shareId: String,
    @ColumnInfo(name = "user_id") val shareUserId: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)