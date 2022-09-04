package com.dinhlam.sharekeeper.ui.home

import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.base.BaseViewModel

data class HomeData(
    val listItem: List<BaseListAdapter.BaseModelView> = emptyList(),
    val isRefreshing: Boolean = false,
    val title: String = "Home"
) : BaseViewModel.BaseData