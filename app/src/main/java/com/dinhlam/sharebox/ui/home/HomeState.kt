package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class HomeState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val generalShares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val boxes: List<BoxDetail> = emptyList(),
) : BaseViewModel.BaseState