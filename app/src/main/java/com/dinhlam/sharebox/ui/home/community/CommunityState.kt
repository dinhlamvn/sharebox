package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail

data class CommunityState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadMore: Boolean = false,
    val voteMap: Map<String, Int> = emptyMap()
) : BaseViewModel.BaseState