package com.dinhlam.sharebox.ui.boxlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.StateManager
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.di.qualifier.BoxListStateManager
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoxListViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    @param:BoxListStateManager
    private val stateManager: StateManager<BoxListState>
) : ViewModel() {

    val state = stateManager.state

    init {
        getListBoxes()
        fetchTotalBox()
    }

    fun reload() {
        stateManager.set(ResetBoxListEvent)
        fetchBoxes(page = 0, append = false)
        fetchTotalBox()
    }

    private fun getListBoxes() = stateManager.get { state ->
        fetchBoxes(state.currentPage, append = false)
    }

    private fun fetchBoxes(page: Int, append: Boolean) {
        stateManager.set(SetBoxListEvent(BaseViewModel.AsyncLoad.Loading, append))
        viewModelScope.launch {
            val result = try {
                BaseViewModel.AsyncLoad.Success(
                    boxRepository.find(
                        AppConsts.NUMBER_VISIBLE_BOX,
                        page * AppConsts.NUMBER_VISIBLE_BOX
                    )
                )
            } catch (error: Throwable) {
                BaseViewModel.AsyncLoad.Failed(error)
            }
            stateManager.set(SetBoxListEvent(result, append))
        }
    }

    private fun fetchTotalBox() {
        viewModelScope.launch {
            val totalBox = try {
                boxRepository.count()
            } catch (_: Throwable) {
                null
            }
            stateManager.set(SetTotalBoxEvent(totalBox.orElse(0)))
        }
    }

    fun loadNextPage() = stateManager.get { state ->
        if (state.boxes.size >= state.totalBox ||
            state.asyncLoadBoxes is BaseViewModel.AsyncLoad.Loading
        ) {
            return@get
        }
        fetchBoxes(state.currentPage, append = true)
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            stateManager.set(SetSearchBoxesEvent(emptyList(), isSearching = false))
            return
        }
        viewModelScope.launch {
            val boxes = try {
                boxRepository.search(query, userHelper.getCurrentUserId())
            } catch (_: Throwable) {
                emptyList()
            }
            stateManager.set(SetSearchBoxesEvent(boxes, isSearching = true))
        }
    }
}
