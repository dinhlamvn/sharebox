package com.dinhlam.sharesaver.dialog.folder.creator

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel

data class FolderCreatorDialogState(
    val folderIdInserted: String? = null,
    val folderName: String = "",
    val folderDesc: String = "",
    val folderPassword: String = "",
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
) : BaseViewModel.BaseState