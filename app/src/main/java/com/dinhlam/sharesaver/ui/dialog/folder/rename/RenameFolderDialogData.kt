package com.dinhlam.sharesaver.ui.dialog.folder.rename

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class RenameFolderDialogData(
    val folder: Folder? = null,
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
    val renameFolderSuccess: Boolean = false,
    val isIgnoreRename: Boolean = false,
) : BaseViewModel.BaseData