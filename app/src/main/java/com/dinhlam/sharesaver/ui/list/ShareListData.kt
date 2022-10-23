package com.dinhlam.sharesaver.ui.list

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Share

data class ShareListData(
    val title: String? = "Shares",
    val folderId: String? = null,
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList(),
) : BaseViewModel.BaseData