package com.dinhlam.sharebox.ui.home.bookmark.creator

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel

data class BookmarkCollectionCreatorState(
    val errorName: Int? = null,
    val errorDesc: Int? = null,
    val errorThumbnail: Boolean = false,
    val thumbnail: Uri? = null,
    val success: Boolean = false,
    val passcode: String = "",
) : BaseViewModel.BaseState