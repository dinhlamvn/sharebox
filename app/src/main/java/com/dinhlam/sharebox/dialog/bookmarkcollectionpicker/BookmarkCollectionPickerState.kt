package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail

data class BookmarkCollectionPickerState(
    val shareId: String,
    val isLoading: Boolean = true,
    val bookmarkCollections: List<BookmarkCollectionDetail> = emptyList(),
    val pickedBookmarkCollectionIds: Set<String> = emptySet(),
    val originalPickedBookmarkCollectionIds: Set<String> = emptySet(),
) : BaseViewModel.BaseState
