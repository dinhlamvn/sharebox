package com.dinhlam.sharebox.ui.discover.pinterest

import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.PinterestPin

data class PinterestDiscoverState(
    val currentBox: BoxDetail? = null,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized,
    val query: String = "",
    val searchUrl: String? = null,
    val pins: List<PinterestPin> = emptyList(),
    val page: Int = 1,
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val asyncSearch: BaseViewModel.AsyncLoad<List<PinterestPin>> =
        BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState
