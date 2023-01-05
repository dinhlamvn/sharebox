package com.dinhlam.sharebox.dialog.tag

import com.dinhlam.sharebox.base.BaseViewModel

data class ChoiceTagState(
    val title: String? = null,
    val selectedTagId: Int = -1
) : BaseViewModel.BaseState
