package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Share

data class HomeData(
    val isRefreshing: Boolean = false,
    val title: String = "Home",
    val shareList: List<Share> = emptyList()
) : BaseViewModel.BaseData