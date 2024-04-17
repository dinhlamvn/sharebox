package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class HomeState(
    val asyncLoadShares: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.Initialize,
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val boxes: List<BoxDetail> = emptyList(),
    val totalBox: Int = 0,
) : BaseViewModel.BaseState