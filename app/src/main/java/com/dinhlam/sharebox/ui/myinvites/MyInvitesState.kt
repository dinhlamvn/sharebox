package com.dinhlam.sharebox.ui.myinvites

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxInvitedData

data class MyInvitesState(
    val loading: Boolean = true,
    val boxList: List<BoxInvitedData> = emptyList()
) : BaseViewModel.BaseState
