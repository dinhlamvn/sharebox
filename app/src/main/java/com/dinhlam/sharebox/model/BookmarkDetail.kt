package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookmarkDetail(
    val id: Int,
    val shareId: String,
    val bookmarkCollectionId: String,
) : Parcelable