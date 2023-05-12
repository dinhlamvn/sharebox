package com.dinhlam.sharebox.database.entity

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
            entity = HashTag::class,
            parentColumns = ["hash_tag_id"],
            childColumns = ["hash_tag_id"]
        )
    ],
    indices = [Index(value = ["share_id", "hash_tag_id"], unique = true)]
)
data class ShareHashTag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("share_id", index = true) val shareId: String,
    @ColumnInfo("hash_tag_id", index = true) val hashTagId: String,
)