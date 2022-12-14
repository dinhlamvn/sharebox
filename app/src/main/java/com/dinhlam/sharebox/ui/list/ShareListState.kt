package com.dinhlam.sharebox.ui.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share

data class ShareListState(
    val title: String? = "Shares",
    val folderId: String? = null,
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList()
) : BaseViewModel.BaseState
