package com.dinhlam.sharebox.ui.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share

data class ShareListState(
    val title: String? = "Shares",
    val folderId: String? = null,
    val isRefreshing: Boolean = true,
    val shareList: List<Share> = emptyList(),
    val searchQuery: String = "",
    val folderName: String = ""
) : BaseViewModel.BaseState
