package com.dinhlam.sharebox.ui.boxdetail

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.BoxTransferManifest
import com.dinhlam.sharebox.model.ShareDetail

data class BoxDetailState(
    val boxDetail: BoxDetail? = null,
    val asyncLoadBoxDetail: BaseViewModel.AsyncLoad<BoxDetail> = BaseViewModel.AsyncLoad.UnInitialized,
    val requirePasscode: Boolean = true,
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false,
    val asyncLoadLoadMoreShares: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadSave: BaseViewModel.AsyncLoad<ShareDetail> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadDeleteBox: BaseViewModel.AsyncLoad<Boolean> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadExportBox: BaseViewModel.AsyncLoad<BoxTransferManifest> =
        BaseViewModel.AsyncLoad.UnInitialized,
    val searchQuery: String? = null,
) : BaseViewModel.BaseState
