package com.dinhlam.sharebox.ui.discover.zingnews

import androidx.collection.LruCache
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ZingNewsCategory
import com.dinhlam.sharebox.model.ZingNewsDiscover

data class ZingNewsDiscoverState(
    val currentBox: BoxDetail? = null,
    val zingNewsCheckedCategories: Set<ZingNewsCategory> = emptySet(),
    val zingNewsCategories: List<ZingNewsCategory> = emptyList(),
    val zingNewsDiscovers: List<ZingNewsDiscover> = emptyList(),
    val asyncLoadZingNewsDiscover: BaseViewModel.AsyncLoad<List<Pair<String, List<ZingNewsDiscover>>>> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized,
    val cache: Map<String, List<ZingNewsDiscover>> = emptyMap()
) : BaseViewModel.BaseState
