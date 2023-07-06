package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BoxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxSelectionDialogViewModel @Inject constructor(private val boxRepository: BoxRepository) :
    BaseViewModel<BoxSelectionDialogState>(BoxSelectionDialogState()) {

    init {
        getListBoxes()
        fetchTotalBox()
    }

    private fun getListBoxes() {
        setState { copy(isLoading = true) }
        execute {
            val boxes = boxRepository.find(
                AppConsts.NUMBER_VISIBLE_BOX, currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
            copy(boxes = boxes, isLoading = false, currentPage = currentPage + 1)
        }
    }

    private fun fetchTotalBox() {
        doInBackground {
            val totalBox = boxRepository.count()
            setState { copy(totalBox = totalBox) }
        }
    }

    fun loadNextPage() = getState { state ->
        if (state.boxes.size == state.totalBox) {
            return@getState
        }
        setState { copy(isLoadingMore = true) }
        execute {
            val boxes = boxRepository.find(
                AppConsts.NUMBER_VISIBLE_BOX, currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
            copy(
                boxes = this.boxes.plus(boxes),
                currentPage = currentPage + 1,
                isLoadingMore = false
            )
        }
    }

    fun search(query: String) {
        if (query.isEmpty()) return setState {
            copy(
                searchBoxes = emptyList(),
                isSearching = false
            )
        }
        doInBackground {
            val searchBoxes = boxRepository.search(query)
            setState { copy(searchBoxes = searchBoxes, isSearching = true) }
        }
    }
}