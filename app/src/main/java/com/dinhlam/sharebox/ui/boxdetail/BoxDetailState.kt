package com.dinhlam.sharebox.ui.boxdetail

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class BoxDetailState(
    val boxId: String,
    val asyncLoadBoxDetail: BaseViewModel.AsyncLoad<BoxDetail> = BaseViewModel.AsyncLoad.Initialize,
    val boxDetail: BoxDetail? = null,
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false,
    val asyncLoadLoadMoreShares: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.Initialize
) : BaseViewModel.BaseState
