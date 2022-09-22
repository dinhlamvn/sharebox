package com.dinhlam.sharesaver.ui.share.dialog

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class ShareFolderPickerDialogData(
    val isFirstLoad: Boolean = true,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val requestCreateFolder: Boolean = false
) : BaseViewModel.BaseData