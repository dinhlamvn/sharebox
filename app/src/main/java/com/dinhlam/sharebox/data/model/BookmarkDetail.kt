package com.dinhlam.sharebox.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookmarkDetail(
    val shareId: String,
    val bookmarkCollectionId: String,
) : Parcelable