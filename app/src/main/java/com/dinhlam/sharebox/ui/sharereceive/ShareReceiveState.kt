package com.dinhlam.sharebox.ui.sharereceive

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.UserDetail

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
