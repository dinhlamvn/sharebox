package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val boxRepository: BoxRepository,
    private val appSettingHelper: AppSettingHelper,
) : BaseViewModel<HomeState>(HomeState(userHelper.getCurrentUserId())) {

    companion object {
        private const val BOX_LIST_INIT_LOAD_SIZE_DEFAULT = 3
    }

    sealed interface Intent : BaseIntent {
        data object Refresh : Intent
        data class Like(val shareId: String) : Intent
        data class SetChooseBoxFor(val chooseBoxFor: HomeState.ChooseBoxFor?) : Intent
        data class RefreshBoxDetail(val boxDetail: BoxDetail) : Intent
    }

    override suspend fun processIntent(intent: BaseIntent) {
        when (intent) {
            is Intent.Refresh -> refreshInternal()
            is Intent.Like -> likeInternal(intent.shareId)
            is Intent.SetChooseBoxFor -> setChooseBoxForInternal(intent.chooseBoxFor)
            is Intent.RefreshBoxDetail -> refreshBoxDetailInternal(intent.boxDetail)
        }
    }

    fun refresh() {
        sendIntent(Intent.Refresh)
    }

    private fun refreshInternal() {
        getTotalBox()
        getListBoxes()
        getRecentlyShares()
    }

    private fun getTotalBox() = suspend {
        boxRepository.count()
    }.execute { asyncLoad ->
        copy(totalBox = asyncLoad.data ?: 0)
    }

    private fun getListBoxes() = suspend {
        boxRepository.find(BOX_LIST_INIT_LOAD_SIZE_DEFAULT, 0)
    }.execute { asyncLoad ->
        copy(boxes = asyncLoad.completed.ifTrue(asyncLoad.data.orEmpty(), boxes))
    }

    private fun getRecentlyShares() {
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                appSettingHelper.getNumOfRecently(),
                0
            )
        }.execute { asyncLoad ->
            val list = asyncLoad.data.orEmpty()
            copy(
                shares = asyncLoad.completed.ifTrue(list, shares),
                isRefreshing = asyncLoad is AsyncLoad.Loading
            )
        }
    }

    fun like(shareId: String) {
        sendIntent(Intent.Like(shareId))
    }

    private fun likeInternal(shareId: String) = setState {
        val shareList = shares.map { shareDetail ->
            if (shareDetail.shareId == shareId) {
                shareDetail.copy(likeNumber = shareDetail.likeNumber + 1, liked = true)
            } else {
                shareDetail
            }
        }
        copy(shares = shareList)
    }

    fun setChooseBoxFor(chooseBoxFor: HomeState.ChooseBoxFor?) {
        sendIntent(Intent.SetChooseBoxFor(chooseBoxFor))
    }

    private fun setChooseBoxForInternal(chooseBoxFor: HomeState.ChooseBoxFor?) = setState {
        copy(chooseBoxFor = chooseBoxFor)
    }

    fun refreshBoxDetail(boxDetail: BoxDetail) {
        sendIntent(Intent.RefreshBoxDetail(boxDetail))
    }

    private fun refreshBoxDetailInternal(boxDetail: BoxDetail) = getState { state ->
        val boxes = state.boxes.map { box ->
            if (box.boxId == boxDetail.boxId) {
                boxDetail
            } else {
                box
            }
        }
        setState { copy(boxes = boxes) }
    }
}
