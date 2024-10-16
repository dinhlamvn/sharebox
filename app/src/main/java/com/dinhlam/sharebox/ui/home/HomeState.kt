package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class HomeState(
    val currentUserId: String,
    val asyncLoadShares: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.Initialize,
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val boxes: List<BoxDetail> = emptyList(),
    val totalBox: Int = 0,
    val chooseBoxFor: ChooseBoxFor? = null,
    val currentShare: ShareDetail? = null,
    val asyncLoadSave: BaseViewModel.AsyncLoad<ShareDetail> = BaseViewModel.AsyncLoad.Initialize
) : BaseViewModel.BaseState {

    sealed interface ChooseBoxFor {
        data object Detail : ChooseBoxFor
        data class Web(val link: String) : ChooseBoxFor
    }
}