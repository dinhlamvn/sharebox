package com.dinhlam.sharebox.ui.boxform

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.model.BoxDetail

data class BoxFormState(
    val boxId: String?,
    val boxDetail: BoxDetail? = null,
    val asyncLoadSave: BaseViewModel.AsyncLoad<Box> = BaseViewModel.AsyncLoad.Initialize,
    val isChangePasscode: Boolean = false
) : BaseViewModel.BaseState
