package com.dinhlam.sharebox.ui.discover.zingnews

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ZingNewsCategory
import com.dinhlam.sharebox.model.ZingNewsDiscover

data class ZingNewsDiscoverState(
    val currentBox: BoxDetail? = null,
    val zingNewsCategories: List<ZingNewsCategory> = ZingNewsCategory.categories.toList(),
    val activeCategory: ZingNewsCategory = ZingNewsCategory.categories.first(),
    val zingNewsDiscovers: List<ZingNewsDiscover> = emptyList(),
    val asyncLoadZingNewsDiscover: BaseViewModel.AsyncLoad<Pair<String, List<ZingNewsDiscover>>> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized,
    val cache: Map<String, List<ZingNewsDiscover>> = emptyMap()
) : BaseViewModel.BaseState
