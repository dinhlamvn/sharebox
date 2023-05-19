package com.dinhlam.sharebox.ui.home.bookmark.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.data.model.ShareDetail

data class BookmarkListItemState(
    val bookmarkCollectionId: String,
    val bookmarkCollection: BookmarkCollectionDetail? = null,
    val requestVerifyPasscode: Boolean = false,
    val isSharesLoading: Boolean = false,
    val shares: List<ShareDetail> = emptyList()
) : BaseViewModel.BaseState
