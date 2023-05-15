package com.dinhlam.sharebox.ui.home.starred

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.ShareDetail

data class StarredState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadMore: Boolean = false,
    val voteMap: Map<String, Int> = emptyMap(),
    val starredSet: Set<String> = emptySet(),
) : BaseViewModel.BaseState