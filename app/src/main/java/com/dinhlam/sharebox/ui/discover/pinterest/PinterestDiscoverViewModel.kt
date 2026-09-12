package com.dinhlam.sharebox.ui.discover.pinterest

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.PinterestRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.model.ShareData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinterestDiscoverViewModel @Inject constructor(
    private val repository: PinterestRepository,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
) : BaseViewModel<PinterestDiscoverState>(PinterestDiscoverState()) {

    init {
        suspend { boxRepository.findLastActiveBox() }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun archiveLink(link: String, note: String?, boxId: String) {
        suspend {
            shareRepository.insert(
                shareData = ShareData.ShareUrl(link),
                shareNote = note,
                shareBoxId = boxId,
            )
            link
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

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
