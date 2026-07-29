package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.AsyncResult
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

data class HomeState(
    val currentUserId: String,
    val asyncLoadShares: AsyncResult<List<ShareDetail>> = AsyncResult.Idle,
    val isRefreshing: Boolean = true,
    val shares: List<ShareDetail> = emptyList(),
    val boxes: List<BoxDetail> = emptyList(),
    val totalBox: Int = 0,
    val chooseBoxFor: ChooseBoxFor? = null,
    val asyncLoadSave: AsyncResult<ShareDetail> = AsyncResult.Idle,
) {

    sealed interface ChooseBoxFor {
        data object Detail : ChooseBoxFor
        data class Web(val link: String) : ChooseBoxFor
    }
}
