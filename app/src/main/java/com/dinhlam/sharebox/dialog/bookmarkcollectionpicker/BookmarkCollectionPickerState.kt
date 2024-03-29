package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail

data class BookmarkCollectionPickerState(
    val shareId: String,
    val collectionId: String?,
    val isLoading: Boolean = true,
    val bookmarkCollections: List<BookmarkCollectionDetail> = emptyList(),
    val pickedBookmarkCollection: BookmarkCollectionDetail? = null,
    val originalBookmarkCollection: BookmarkCollectionDetail? = null,
) : BaseViewModel.BaseState
