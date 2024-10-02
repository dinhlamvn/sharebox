package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.model.BoxDetail

data class ShareLinkState(
    val hasLinkExtra: Boolean = false,
    val currentBox: BoxDetail? = null,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.Initialize
) : BaseViewModel.BaseState