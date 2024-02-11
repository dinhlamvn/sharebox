package com.dinhlam.sharebox.ui.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.UserDetail

data class ProfileState(
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val currentUser: UserDetail? = null,
) : BaseViewModel.BaseState