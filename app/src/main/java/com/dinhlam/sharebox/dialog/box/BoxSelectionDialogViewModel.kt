package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxSelectionDialogViewModel @Inject constructor(
    private val boxRepository: BoxRepository, private val userHelper: UserHelper
) : BaseViewModel<BoxSelectionDialogState>(BoxSelectionDialogState()) {

    init {
        getListBoxes()
        fetchTotalBox()
    }

    private fun getListBoxes() = getState { state ->
        setState { copy(isLoading = true) }
        suspend {
            boxRepository.findByUser(
                userHelper.getCurrentUserId(),
                AppConsts.NUMBER_VISIBLE_BOX,
                state.currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
        }.execute { boxes ->
            copy(boxes = boxes, isLoading = false, currentPage = currentPage + 1)
        }
    }

    private fun fetchTotalBox() {
        doInBackground {
            suspend { boxRepository.count(userHelper.getCurrentUserId()) }.execute { total ->
                copy(totalBox = total)
            }
        }
    }

    fun loadNextPage() = getState { state ->
        if (state.boxes.size == state.totalBox) {
            return@getState
        }
        setState { copy(isLoadingMore = true) }
        suspend {
            boxRepository.findByUser(
                userHelper.getCurrentUserId(),
                AppConsts.NUMBER_VISIBLE_BOX,
                state.currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
        }.execute { list ->
            copy(
                boxes = this.boxes.plus(list), currentPage = currentPage + 1, isLoadingMore = false
            )
        }
    }

    fun search(query: String) {
        if (query.isEmpty()) return setState {
            copy(
                searchBoxes = emptyList(), isSearching = false
            )
        }
        doInBackground {
            val searchBoxes = boxRepository.search(query, userHelper.getCurrentUserId())
            setState { copy(searchBoxes = searchBoxes, isSearching = true) }
        }
    }
}