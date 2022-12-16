package com.dinhlam.sharebox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            onDelete = ForeignKey.CASCADE,
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"]
        )
    ]
)
data class Share(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "folder_id", index = true) val folderId: String,
    @ColumnInfo(name = "share_type") val shareType: String,
    @ColumnInfo(name = "share_info") val shareInfo: String,
    @ColumnInfo(name = "share_note") val shareNote: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
