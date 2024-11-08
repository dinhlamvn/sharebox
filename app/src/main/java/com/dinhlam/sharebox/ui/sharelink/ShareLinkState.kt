package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share

data class ShareLinkState(
    val hasLinkExtra: Boolean = false,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.Initialize,
    val linkError: String? = null,
) : BaseViewModel.BaseState