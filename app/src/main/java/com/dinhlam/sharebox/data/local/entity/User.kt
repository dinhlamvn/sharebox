package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis

@Entity(
    indices = [Index(value = ["user_id"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("user_id") val userId: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("avatar") val avatar: String,
    @ColumnInfo("level") val level: Int = 0,
    @ColumnInfo("drama") val drama: Int = 0,
    @ColumnInfo("join_date") val joinDate: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long = nowUTCTimeInMillis(),
)