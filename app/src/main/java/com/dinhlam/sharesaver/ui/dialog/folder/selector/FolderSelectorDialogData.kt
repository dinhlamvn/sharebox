package com.dinhlam.sharesaver.ui.dialog.folder.selector

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class FolderSelectorDialogData(
    val isFirstLoad: Boolean = true,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val requestCreateFolder: Boolean = false
) : BaseViewModel.BaseData