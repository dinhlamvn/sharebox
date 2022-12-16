package com.dinhlam.sharebox.dialog.folder.selector

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class FolderSelectorDialogState(
    val isFirstLoad: Boolean = true,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val requestCreateFolder: Boolean = false
) : BaseViewModel.BaseState
