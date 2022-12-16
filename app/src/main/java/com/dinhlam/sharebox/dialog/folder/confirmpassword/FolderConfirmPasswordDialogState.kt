package com.dinhlam.sharebox.dialog.folder.confirmpassword

import androidx.annotation.StringRes
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class FolderConfirmPasswordDialogState(
    val folder: Folder? = null,
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
    val verifyPasswordSuccess: Boolean = false
) : BaseViewModel.BaseState
