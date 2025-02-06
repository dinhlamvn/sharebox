package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.TiktokCategory
import com.dinhlam.sharebox.model.TiktokDiscover

data class TiktokDiscoverState(
    val currentBox: BoxDetail? = null,
    val categories: List<TiktokCategory> = TiktokCategory.categories,
    val activeCategory: TiktokCategory = TiktokCategory.categories.first(),
    val tiktokDiscoverList: List<TiktokDiscover> = emptyList(),
    val asyncLoadTiktokDiscover: BaseViewModel.AsyncLoad<List<TiktokDiscover>> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncLoadArchive: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized,
    val tiktokDownloadUrlCache: Map<String, String> = emptyMap(),
    val isLoadingMore: Boolean = false,
) : BaseViewModel.BaseState
