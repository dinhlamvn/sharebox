package com.dinhlam.sharesaver.dialog.folder.detail

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class FolderDetailDialogState(
    val folder: Folder? = null,
    val shareCount: Int = 0
) : BaseViewModel.BaseState
