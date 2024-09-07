package com.dinhlam.sharebox.ui.boxmember

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxMember

data class BoxMemberState(
    val boxId: String,
    val members: List<BoxMember> = emptyList()
) : BaseViewModel.BaseState
