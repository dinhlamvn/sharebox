package com.dinhlam.sharesaver.ui.share.dialog.foldercreator

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel

data class ShareFolderCreatorDialogData(
    val folderIdInserted: String? = null,
    val folderName: String = "",
    val folderDesc: String = "",
    val folderPassword: String = "",
    @StringRes val error: Int = 0,
    @StringRes val toastRes: Int = 0,
) : BaseViewModel.BaseData