package com.dinhlam.sharebox.ui.bookmark.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.ShareDetail

data class BookmarkListItemState(
    val bookmarkCollectionId: String,
    val bookmarkCollection: BookmarkCollectionDetail? = null,
    val requestVerifyPasscode: Boolean = false,
    val isSharesLoading: Boolean = false,
    val shares: List<ShareDetail> = emptyList()
) : BaseViewModel.BaseState
