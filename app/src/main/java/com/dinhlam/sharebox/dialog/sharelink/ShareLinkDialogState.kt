package com.dinhlam.sharebox.dialog.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail

data class ShareLinkDialogState(
    val currentBox: BoxDetail? = null
): BaseViewModel.BaseState