package com.dinhlam.sharesaver.ui.home

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.database.entity.Share

data class HomeData(
    val showProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val title: String = "Home",
    val shareList: List<Share> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    @StringRes val toastRes: Int = 0,
    val folderDeleteConfirmation: FolderDeleteConfirmation? = null
) : BaseViewModel.BaseData {
    data class FolderDeleteConfirmation(
        val folder: Folder,
        val shareCount: Int
    )
}