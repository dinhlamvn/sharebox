package com.dinhlam.sharebox.ui.home.bookmark.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail

data class BookmarkListItemState(
    val bookmarkCollectionId: String,
    val bookmarkCollection: BookmarkCollectionDetail? = null
) : BaseViewModel.BaseState
