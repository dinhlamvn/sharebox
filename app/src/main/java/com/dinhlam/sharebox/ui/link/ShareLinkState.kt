package com.dinhlam.sharebox.ui.link

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share

data class ShareLinkState(
    val hasLinkExtra: Boolean = false,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.UnInitialized,
    val linkError: String? = null,
) : BaseViewModel.BaseState