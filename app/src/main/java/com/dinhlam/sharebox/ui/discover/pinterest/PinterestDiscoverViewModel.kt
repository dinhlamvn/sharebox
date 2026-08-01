package com.dinhlam.sharebox.ui.discover.pinterest

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.PinterestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinterestDiscoverViewModel @Inject constructor(
    private val repository: PinterestRepository,
) : BaseViewModel<PinterestDiscoverState>(PinterestDiscoverState()) {

    fun search(input: String) {
        val query = input.trim()
        if (query.isBlank()) {
            return
        }
        suspend { repository.search(query) }.execute { asyncLoad ->
            copy(
                query = query,
                searchUrl = repository.buildSearchUrl(query),
                pins = asyncLoad.data ?: if (asyncLoad is AsyncLoad.Loading) emptyList() else pins,
                page = 1,
                canLoadMore = asyncLoad.data?.isNotEmpty() ?: true,
                isLoadingMore = false,
                asyncSearch = asyncLoad,
            )
        }
    }

    fun loadMore() = getState { state ->
        if (
            state.query.isBlank() ||
            state.isLoadingMore ||
            !state.canLoadMore ||
            state.asyncSearch is AsyncLoad.Loading
        ) {
            return@getState
        }

        val nextPage = state.page + 1
        suspend { repository.search(state.query, nextPage) }.execute { asyncLoad ->
            when (asyncLoad) {
                is AsyncLoad.Success -> {
                    val newPins = asyncLoad.value.filterNot { newPin ->
                        pins.any { it.id == newPin.id }
                    }
                    copy(
                        pins = pins + newPins,
                        page = nextPage,
                        canLoadMore = newPins.isNotEmpty(),
                        isLoadingMore = false,
                    )
                }

                is AsyncLoad.Loading -> copy(isLoadingMore = true)
                else -> copy(isLoadingMore = false)
            }
        }
    }

    fun refresh() = getState { state ->
        if (state.query.isNotBlank()) {
            search(state.query)
        }
    }
}
