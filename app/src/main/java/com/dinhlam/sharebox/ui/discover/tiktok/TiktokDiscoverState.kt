package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.TiktokDiscover

data class TiktokDiscoverState(
    val currentBox: BoxDetail? = null,
    val tiktokDiscoverList: List<TiktokDiscover> = emptyList(),
    val asyncLoadTiktokDiscover: BaseViewModel.AsyncLoad<List<TiktokDiscover>> = BaseViewModel.AsyncLoad.Initialize,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.Initialize
) : BaseViewModel.BaseState
