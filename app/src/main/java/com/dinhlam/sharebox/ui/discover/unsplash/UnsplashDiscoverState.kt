package com.dinhlam.sharebox.ui.discover.unsplash

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.UnsplashPhoto

data class UnsplashDiscoverState(
    val query: String = "",
    val photos: List<UnsplashPhoto> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 1,
    val isLoadingMore: Boolean = false,
    val loadMoreError: String? = null,
    val searchRequestId: Long = 0,
    val downloadingPhotoId: String? = null,
    val asyncDownload: BaseViewModel.AsyncLoad<String> = BaseViewModel.AsyncLoad.UnInitialized,
    val asyncSearch: BaseViewModel.AsyncLoad<List<UnsplashPhoto>> =
        BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState {
    val canLoadMore: Boolean
        get() = query.isNotBlank() && photos.isNotEmpty() &&
            asyncSearch is BaseViewModel.AsyncLoad.Success &&
            !isLoadingMore && page > 0 && page < totalPages

    fun shouldLoadMore(lastVisibleIndex: Int): Boolean =
        canLoadMore && loadMoreError == null && lastVisibleIndex >= 0 &&
            lastVisibleIndex >= photos.lastIndex - 4
}
