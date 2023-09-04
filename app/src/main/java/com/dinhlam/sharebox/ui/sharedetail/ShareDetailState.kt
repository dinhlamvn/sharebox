package com.dinhlam.sharebox.ui.sharedetail

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.CommentDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.UserDetail

data class ShareDetailState(
    val shareId: String,
    val share: ShareDetail? = null,
    val comments: List<CommentDetail> = emptyList(),
    val currentUser: UserDetail? = null,
) : BaseViewModel.BaseState