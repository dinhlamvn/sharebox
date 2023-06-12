package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BoxDetail

data class BoxSelectionDialogState(
    val isLoading: Boolean = false,
    val boxes: List<BoxDetail> = emptyList()
) : BaseViewModel.BaseState
