package com.dinhlam.sharebox.ui.comment

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.CommentDetail
import com.dinhlam.sharebox.model.UserDetail

data class CommentState(
    val shareId: String,
    val currentUser: UserDetail? = null,
    val isRefreshing: Boolean = true,
    val comments: List<CommentDetail> = emptyList(),
) : BaseViewModel.BaseState
