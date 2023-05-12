package com.dinhlam.sharebox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = Share::class,
        parentColumns = ["share_id"],
        childColumns = ["share_id"]
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"]
    )], indices = [Index(value = ["share_id", "user_id"], unique = true)]
)
data class Vote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("share_id") val shareId: String,
    @ColumnInfo("user_id") val userId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
