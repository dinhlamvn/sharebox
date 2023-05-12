package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.UserDetail

data class ProfileState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadMore: Boolean = false,
    val activeUser: UserDetail? = null,
) : BaseViewModel.BaseState