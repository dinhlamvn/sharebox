package com.dinhlam.sharebox.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookmarkCollectionDetail(
    val id: String,
    val name: String,
    val thumbnail: String,
    val desc: String,
    val passcode: String?,
) : Parcelable