package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Share
import com.dinhlam.sharesaver.model.Folder

data class HomeData(
    val isRefreshing: Boolean = false,
    val title: String = "Home",
    val shareList: List<Share> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedShareType: String = ""
) : BaseViewModel.BaseData