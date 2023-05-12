package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.model.ShareDetail

data class ProfileState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val shareModelViews: List<BaseListAdapter.BaseModelView> = emptyList(),
    val isLoadMore: Boolean = false,
    val activeUser: User? = null,
) : BaseViewModel.BaseState