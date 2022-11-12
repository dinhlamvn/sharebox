package com.dinhlam.sharesaver.dialog.tag

import com.dinhlam.sharesaver.base.BaseViewModel

data class ChoiceTagState(
    val title: String? = null,
    val selectedPosition: Int = -1
) : BaseViewModel.BaseState
