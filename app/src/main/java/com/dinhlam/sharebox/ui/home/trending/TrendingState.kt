package com.dinhlam.sharebox.ui.home.trending

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class TrendingState(
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val currentBox: BoxDetail? = null,
    val showLoading: Boolean = false,
) : BaseViewModel.BaseState