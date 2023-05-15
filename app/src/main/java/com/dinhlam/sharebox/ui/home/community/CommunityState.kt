package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.ShareDetail

data class CommunityState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadMore: Boolean = false,
    val voteMap: Map<String, Int> = emptyMap(),
    val starredSet: Set<String> = emptySet(),
) : BaseViewModel.BaseState