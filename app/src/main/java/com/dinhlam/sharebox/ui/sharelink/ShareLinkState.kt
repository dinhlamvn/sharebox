package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail

data class ShareLinkState(
    val hasLinkExtra: Boolean = false,
    val currentBox: BoxDetail? = null
): BaseViewModel.BaseState