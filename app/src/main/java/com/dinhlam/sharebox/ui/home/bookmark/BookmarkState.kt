package com.dinhlam.sharebox.ui.home.bookmark

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail

data class BookmarkState(
    val isRefreshing: Boolean = false,
    val bookmarkCollections: List<BookmarkCollectionDetail> = emptyList(),
) : BaseViewModel.BaseState