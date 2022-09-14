package com.dinhlam.sharekeeper.ui.home

import com.dinhlam.sharekeeper.base.BaseViewModel
import com.dinhlam.sharekeeper.database.entity.Share

data class HomeData(
    val isRefreshing: Boolean = false,
    val title: String = "Home",
    val shareList: List<Share> = emptyList()
) : BaseViewModel.BaseData