package com.dinhlam.keepmyshare.ui.home

import com.dinhlam.keepmyshare.base.BaseListAdapter
import com.dinhlam.keepmyshare.base.BaseViewModel

data class HomeData(
    val listItem: List<BaseListAdapter.BaseModelView> = emptyList(),
    val isRefreshing: Boolean = false,
    val title: String = "Home"
) : BaseViewModel.BaseData