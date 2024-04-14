package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.extensions.orElse
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
        suspend {
            boxRepository.findByUser(
                userHelper.getCurrentUserId(),
                AppConsts.NUMBER_VISIBLE_BOX,
                state.currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxes = asyncLoad,
                boxes = asyncLoad.data.orEmpty(),
                currentPage = currentPage + 1
            )
        }
    }

    private fun fetchTotalBox() {
        doInBackground {
            suspend { boxRepository.count(userHelper.getCurrentUserId()) }.execute { asyncLoad ->
                copy(totalBox = asyncLoad.data.orElse(0))
            }
        }
    }

    fun loadNextPage() = getState { state ->
        if (state.boxes.size == state.totalBox) {
            return@getState
        }
        suspend {
            boxRepository.findByUser(
                userHelper.getCurrentUserId(),
                AppConsts.NUMBER_VISIBLE_BOX,
                state.currentPage * AppConsts.NUMBER_VISIBLE_BOX
            )
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxes = asyncLoad,
                boxes = this.boxes.plus(asyncLoad.data.orEmpty()),
                currentPage = currentPage + 1
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