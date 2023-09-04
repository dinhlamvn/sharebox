package com.dinhlam.sharebox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis

@Entity(
    tableName = "box",
    indices = [Index(value = ["box_id"], unique = true), Index(
        value = ["box_name"],
        unique = true
    ), Index(value = ["created_by"])]
)
data class Box(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "box_id") val boxId: String,
    @ColumnInfo(name = "box_name") val boxName: String,
    @ColumnInfo(name = "box_desc") val boxDesc: String?,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "created_date") val createdDate: Long,
    @ColumnInfo(name = "passcode") val passcode: String?,
    @ColumnInfo(name = "last_seen") val lastSeen: Long,
    @ColumnInfo(name = "synced", defaultValue = "0") val synced: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long = nowUTCTimeInMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = nowUTCTimeInMillis()
)
