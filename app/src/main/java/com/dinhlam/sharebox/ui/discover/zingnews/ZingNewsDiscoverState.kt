package com.dinhlam.sharebox.ui.discover.zingnews

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ZingNewsCategory
import com.dinhlam.sharebox.model.ZingNewsDiscover

data class ZingNewsDiscoverState(
    val currentBox: BoxDetail? = null,
    val zingNewsCategory: ZingNewsCategory? = null,
    val zingNewsCategories: List<ZingNewsCategory> = emptyList(),
    val zingNewsDiscovers: List<ZingNewsDiscover> = emptyList(),
    val asyncLoadZingNewsDiscover: BaseViewModel.AsyncLoad<List<ZingNewsDiscover>> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized
) : BaseViewModel.BaseState
