package com.dinhlam.sharebox.dialog.folder.detail

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class FolderDetailDialogState(
    val folder: Folder? = null,
    val shareCount: Int = 0
) : BaseViewModel.BaseState
