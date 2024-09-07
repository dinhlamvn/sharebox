package com.dinhlam.sharebox.ui.boxinvited

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail

data class BoxInvitedState(
    val loading: Boolean = true,
    val boxList: List<BoxDetail> = emptyList()
) : BaseViewModel.BaseState
