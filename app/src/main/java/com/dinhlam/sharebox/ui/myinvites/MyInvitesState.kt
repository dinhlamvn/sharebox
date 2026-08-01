package com.dinhlam.sharebox.ui.myinvites

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.model.BoxInvitedData
import com.dinhlam.sharebox.model.BoxTransferManifest

data class MyInvitesState(
    val loading: Boolean = true,
    val boxList: List<BoxInvitedData> = emptyList(),
    val importBox: BaseViewModel.AsyncLoad<Box> = BaseViewModel.AsyncLoad.UnInitialized,
    val exportBox: BaseViewModel.AsyncLoad<BoxTransferManifest> =
        BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState
