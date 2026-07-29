package com.dinhlam.sharebox.ui.discover.pinterest

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.PinterestPin

data class PinterestDiscoverState(
    val query: String = "",
    val searchUrl: String? = null,
    val pins: List<PinterestPin> = emptyList(),
    val page: Int = 1,
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val asyncSearch: BaseViewModel.AsyncLoad<List<PinterestPin>> =
        BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState
