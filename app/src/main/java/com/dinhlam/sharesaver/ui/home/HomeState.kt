package com.dinhlam.sharesaver.ui.home

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.database.entity.Share

data class HomeState(
    val showProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val folderToOpen: Folder? = null,
    @StringRes val toastRes: Int = 0,
    val folderActionConfirmation: FolderActionConfirmation? = null,
    val folderPasswordConfirmRemind: Set<String> = emptySet(),
    val tag: Int? = null
) : BaseViewModel.BaseState {
    data class FolderActionConfirmation(
        val folder: Folder,
        val shareCount: Int,
        val folderActionType: FolderActionType,
        val ignorePassword: Boolean = false
    ) {
        enum class FolderActionType {
            OPEN,
            DELETE,
            RENAME,
            DETAIL,
            TAG
        }
    }
}