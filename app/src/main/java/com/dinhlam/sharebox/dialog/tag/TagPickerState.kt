package com.dinhlam.sharebox.dialog.tag

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.Tag

data class TagPickerState(
    val shareId: String,
    val tags: List<Tag> = emptyList(),
    val tagIdPicked: Int? = null,
    val asyncLoadSaveTag: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.UnInitialized
) : BaseViewModel.BaseState
