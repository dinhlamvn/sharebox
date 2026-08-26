package com.dinhlam.sharebox.ui.discover.unsplash

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.UnsplashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnsplashDiscoverViewModel @Inject constructor(
    private val repository: UnsplashRepository,
) : BaseViewModel<UnsplashDiscoverState>(UnsplashDiscoverState()) {

    fun search(input: String) {
        val query = input.trim()
        if (query.isBlank()) return

        getState { state ->
            val requestId = state.searchRequestId + 1
            suspend { repository.search(query, page = 1) }.execute { asyncLoad ->
                if (asyncLoad !is AsyncLoad.Loading && searchRequestId != requestId) {
                    return@execute this
                }
                copy(
                    searchRequestId = requestId,
                    query = query,
                    photos = asyncLoad.data?.photos
                        ?: if (asyncLoad is AsyncLoad.Loading) emptyList() else photos,
                    page = if (asyncLoad is AsyncLoad.Success) 1 else 0,
                    totalPages = asyncLoad.data?.totalPages ?: 0,
                    isLoadingMore = false,
                    loadMoreError = null,
                    asyncSearch = when (asyncLoad) {
                        is AsyncLoad.Success -> AsyncLoad.Success(asyncLoad.value.photos)
                        is AsyncLoad.Failed -> asyncLoad
                        is AsyncLoad.Loading -> AsyncLoad.Loading
                        is AsyncLoad.UnInitialized -> AsyncLoad.UnInitialized
                    },
                )
            }
        }
    }

    fun loadMore() = getState { state ->
        if (!state.canLoadMore) return@getState

        val nextPage = state.page + 1
        suspend { repository.search(state.query, nextPage) }.execute { asyncLoad ->
            if (searchRequestId != state.searchRequestId) return@execute this
            when (asyncLoad) {
                is AsyncLoad.Loading -> copy(isLoadingMore = true, loadMoreError = null)
                is AsyncLoad.Success -> copy(
                    photos = (photos + asyncLoad.value.photos).distinctBy { it.id },
                    page = nextPage,
                    totalPages = if (asyncLoad.value.photos.isEmpty()) nextPage
                        else asyncLoad.value.totalPages,
                    isLoadingMore = false,
                    loadMoreError = null,
                )
                is AsyncLoad.Failed -> copy(
                    isLoadingMore = false,
                    loadMoreError = asyncLoad.error.message ?: "Unable to load more photos",
                )
                else -> copy(isLoadingMore = false)
            }
        }
    }

    fun retry() = search(currentState.query)

    fun download(photo: com.dinhlam.sharebox.model.UnsplashPhoto) {
        if (currentState.downloadingPhotoId != null) return
        suspend {
            repository.download(photo)
            photo.id
        }.execute { asyncLoad ->
            copy(
                downloadingPhotoId = if (asyncLoad is AsyncLoad.Loading) photo.id else null,
                asyncDownload = asyncLoad,
            )
        }
    }
}
