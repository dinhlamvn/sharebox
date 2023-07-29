package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.model.VideoSource

@Entity(
    tableName = "video_mixer",
    indices = [
        Index(value = ["share_id"], unique = true),
    ]
)
data class VideoMixer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "share_id") val shareId: String,
    @ColumnInfo(name = "original_url") val originalUrl: String,
    @ColumnInfo(name = "source") val source: VideoSource,
    @ColumnInfo(name = "source_id") val sourceId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
