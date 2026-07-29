package com.dinhlam.sharebox.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val boxRepository: BoxRepository,
    private val appSettingHelper: AppSettingHelper,
) : ViewModel() {

    companion object {
        private const val BOX_LIST_INIT_LOAD_SIZE_DEFAULT = 3
    }

    private val stateManager = HomeStateManager(HomeState(userHelper.getCurrentUserId()))

    val state = stateManager.state
    val currentState: HomeState
        get() = stateManager.value

    fun refresh() {
        stateManager.refreshing()
        loadTotalBox()
        loadBoxes()
        loadRecentlyShared()
    }

    private fun loadTotalBox() = viewModelScope.launch {
        runCatching { boxRepository.count() }
            .onSuccess(stateManager::totalBoxLoaded)
    }

    private fun loadBoxes() = viewModelScope.launch {
        runCatching { boxRepository.find(BOX_LIST_INIT_LOAD_SIZE_DEFAULT, 0) }
            .onSuccess(stateManager::boxesLoaded)
    }

    private fun loadRecentlyShared() = viewModelScope.launch {
        runCatching {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                appSettingHelper.getNumOfRecently(),
                0
            )
        }.onSuccess(stateManager::sharesLoaded)
            .onFailure(stateManager::sharesFailed)
    }

    fun like(shareId: String) {
        stateManager.shareLiked(shareId)
    }

    fun setChooseBoxFor(chooseBoxFor: HomeState.ChooseBoxFor?) {
        stateManager.chooseBoxFor(chooseBoxFor)
    }

    fun refreshBoxDetail(boxDetail: BoxDetail) {
        stateManager.boxUpdated(boxDetail)
    }
}
