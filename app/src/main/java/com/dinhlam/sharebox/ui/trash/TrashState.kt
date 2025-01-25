package com.dinhlam.sharebox.ui.trash

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail

data class TrashState(
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false,
    val asyncLoadLoadMoreShares: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.UnInitialized,
    val currentShare: ShareDetail? = null
) : BaseViewModel.BaseState
