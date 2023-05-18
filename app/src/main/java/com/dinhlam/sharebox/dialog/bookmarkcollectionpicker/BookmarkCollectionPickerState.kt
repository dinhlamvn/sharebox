package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail

data class BookmarkCollectionPickerState(
    val shareId: String,
    val isLoading: Boolean = true,
    val bookmarkCollections: List<BookmarkCollectionDetail> = emptyList(),
    val pickedBookmarkCollection: BookmarkCollectionDetail? = null,
    val originalBookmarkCollection: BookmarkCollectionDetail? = null,
) : BaseViewModel.BaseState
