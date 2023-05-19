package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "share_community",
    foreignKeys = [ForeignKey(
        entity = Share::class, parentColumns = ["share_id"], childColumns = ["share_id"]
    )],
    indices = [
        Index(value = ["share_id"], unique = true),
    ]
)
data class ShareCommunity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "share_id") val shareId: String,
    @ColumnInfo(name = "share_power") val sharePower: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
