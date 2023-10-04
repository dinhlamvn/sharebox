package com.dinhlam.sharebox.model

import androidx.room.ColumnInfo

data class TrendingShare(
    @ColumnInfo("share_id") val shareId: String,
    @ColumnInfo("score") val score: Int
)