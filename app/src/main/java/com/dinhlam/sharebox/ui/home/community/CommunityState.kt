package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.data.model.ShareDetail

data class CommunityState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val currentBox: Box = Box.All
) : BaseViewModel.BaseState