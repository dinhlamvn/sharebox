package com.dinhlam.sharebox.data.local.entity

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
        entity = BookmarkCollection::class,
        parentColumns = ["id"],
        childColumns = ["bookmark_collection_id"]
    )],
    indices = [Index(
        value = ["bookmark_collection_id", "share_id"],
        unique = true
    ), Index(value = ["share_id"])]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("bookmark_collection_id") val bookmarkCollectionId: String,
    @ColumnInfo("share_id") val shareId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
