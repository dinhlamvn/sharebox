package com.dinhlam.sharebox.ui.sharetext

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail

data class ShareTextState(
    val shareId: String? = null,
    val shareDetail: ShareDetail? = null,
    val asyncLoadSave: BaseViewModel.AsyncLoad<Boolean> = BaseViewModel.AsyncLoad.Initialize
) : BaseViewModel.BaseState
