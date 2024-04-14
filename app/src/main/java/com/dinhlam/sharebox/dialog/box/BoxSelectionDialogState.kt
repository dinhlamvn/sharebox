package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail

data class BoxSelectionDialogState(
    val asyncLoadBoxes: BaseViewModel.AsyncLoad<List<BoxDetail>> = BaseViewModel.AsyncLoad.Initialize,
    val boxes: List<BoxDetail> = emptyList(),
    val searchBoxes: List<BoxDetail> = emptyList(),
    val totalBox: Int = 0,
    val currentPage: Int = 0,
    val isLoadingMore: Boolean = false,
    val isSearching: Boolean = false
) : BaseViewModel.BaseState
