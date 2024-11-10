package com.dinhlam.sharebox.ui.boxmember

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.BoxMember

data class BoxMemberState(
    val boxId: String,
    val members: List<BoxMember> = emptyList(),
    val loading: Boolean = true,
    val boxDetail: BoxDetail? = null
) : BaseViewModel.BaseState
