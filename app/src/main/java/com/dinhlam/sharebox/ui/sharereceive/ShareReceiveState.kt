package com.dinhlam.sharebox.ui.sharereceive

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail

data class ShareReceiveState(
    val activeUser: UserDetail? = null,
    val shareData: ShareData? = null,
    val isSaveSuccess: Boolean = false,
    val note: String? = null,
    val requestPassword: Boolean = false,
    val bookmarkCollection: BookmarkCollectionDetail? = null,
    val showLoading: Boolean = false,
    val currentBox: BoxDetail? = null,
    val boxes: List<BoxDetail> = emptyList()
) : BaseViewModel.BaseState
