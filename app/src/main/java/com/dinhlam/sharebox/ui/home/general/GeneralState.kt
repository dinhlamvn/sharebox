package com.dinhlam.sharebox.ui.home.general

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class GeneralState(
    val isRefreshing: Boolean = false,
    val shares: List<ShareDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val currentBox: BoxDetail? = null,
    val boxes: List<BoxDetail> = emptyList(),
) : BaseViewModel.BaseState