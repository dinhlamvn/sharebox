package com.dinhlam.sharesaver.dialog.folder.confirmpassword

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class FolderConfirmPasswordDialogState(
    val folder: Folder? = null,
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
    val verifyPasswordSuccess: Boolean = false
) : BaseViewModel.BaseState
