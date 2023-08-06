package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.UserDetail

data class ProfileState(
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentUser: UserDetail? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val boxes: List<BoxDetail> = emptyList(),
    val currentBox: BoxDetail? = null,
) : BaseViewModel.BaseState