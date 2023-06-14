package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BoxDetail

data class BoxSelectionDialogState(
    val isLoading: Boolean = false,
    val boxes: List<BoxDetail> = emptyList(),
    val searchBoxes: List<BoxDetail> = emptyList(),
    val totalBox: Int = 0,
    val currentPage: Int = 0,
    val isLoadingMore: Boolean = false,
    val isSearching: Boolean = false
) : BaseViewModel.BaseState
