package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share

data class ProfileState(
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList(),
    val shareModelViews: List<BaseListAdapter.BaseModelView> = emptyList(),
    val isLoadMore: Boolean = false
) : BaseViewModel.BaseState