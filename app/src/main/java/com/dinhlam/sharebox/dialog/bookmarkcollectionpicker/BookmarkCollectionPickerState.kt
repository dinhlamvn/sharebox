package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail

data class BookmarkCollectionPickerState(
    val shareId: String,
    val isLoading: Boolean = true,
    val bookmarkCollections: List<BookmarkCollectionDetail> = emptyList(),
    val pickedBookmarkCollectionId: String? = null,
    val passcode: String? = null,
    val originalPickedBookmarkCollectionId: String? = null,
) : BaseViewModel.BaseState
