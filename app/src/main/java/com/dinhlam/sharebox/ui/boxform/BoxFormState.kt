package com.dinhlam.sharebox.ui.boxform

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.model.BoxDetail

data class BoxFormState(
    val currentBoxDetail: BoxDetail? = null,
    val asyncLoadSave: BaseViewModel.AsyncLoad<Box> = BaseViewModel.AsyncLoad.UnInitialized,
    val isUsePasscode: Boolean = false,
    val isPasscodeVisible: Boolean = false,
) : BaseViewModel.BaseState
