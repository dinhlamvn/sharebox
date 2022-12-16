package com.dinhlam.sharebox.dialog.folder.rename

import androidx.annotation.StringRes
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class RenameFolderDialogState(
    val folder: Folder? = null,
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
    val renameFolderSuccess: Boolean = false,
    val isIgnoreRename: Boolean = false
) : BaseViewModel.BaseState
