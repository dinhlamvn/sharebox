package com.dinhlam.sharebox.ui.home

import androidx.annotation.StringRes
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.SortType

data class HomeState(
    val showProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val shareList: List<Share> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val folderToOpen: Folder? = null,
    @StringRes val toastRes: Int = 0,
    val folderActionConfirmation: FolderActionConfirmation? = null,
    val folderPasswordConfirmRemind: Set<String> = emptySet(),
    val tag: Int? = null,
    val sortType: SortType = SortType.NONE,
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
