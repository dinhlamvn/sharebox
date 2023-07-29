package com.dinhlam.sharebox.ui.home.bookmark.form

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail

data class BookmarkCollectionFormState(
    val bookmarkCollectionDetail: BookmarkCollectionDetail? = null,
    val errorName: Int? = null,
    val errorDesc: Int? = null,
    val errorThumbnail: Boolean = false,
    val thumbnail: Uri? = null,
    val success: Boolean = false,
    val passcode: String? = null,
    val isPasscodeVisible: Boolean = false
) : BaseViewModel.BaseState