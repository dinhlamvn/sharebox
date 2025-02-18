package com.dinhlam.sharebox.ui.tags

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Tag
import com.dinhlam.sharebox.model.ShareDetail

data class TagsState(
    val tags: List<Tag> = emptyList(),
    val tagActive: Tag? = null,
    val shares: List<ShareDetail> = emptyList(),
    val asyncLoadShare: BaseViewModel.AsyncLoad<List<ShareDetail>> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState
