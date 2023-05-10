package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.ui.share.ShareState

data class CommunityState(
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList(),
    val shareModelViews: List<BaseListAdapter.BaseModelView> = emptyList(),
    val isLoadMore: Boolean = false
) : BaseViewModel.BaseState