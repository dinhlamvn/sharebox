package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["hash_tag_id"], unique = true)]
)
data class HashTag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("hash_tag_id") val hashTagId: String,
    @ColumnInfo("hash_tag_name") val hashTagName: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
