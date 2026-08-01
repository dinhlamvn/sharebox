package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.AsyncResult
import com.dinhlam.sharebox.base.StateManager
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail

/**
 * Home-specific state mutations live here, outside HomeViewModel.
 */
class HomeStateManager(initialState: HomeState) {

    private val manager = StateManager(initialState)

    val state = manager.state
    val value: HomeState
        get() = manager.value

    fun refreshing() = manager.update {
        copy(isRefreshing = true)
    }

    fun sharesLoaded(shares: List<ShareDetail>) = manager.update {
        copy(
            shares = shares,
            isRefreshing = false,
            asyncLoadShares = AsyncResult.Success(shares)
        )
    }

    fun sharesFailed(error: Throwable) = manager.update {
        copy(isRefreshing = false, asyncLoadShares = AsyncResult.Failure(error))
    }

    fun boxesLoaded(boxes: List<BoxDetail>) = manager.update {
        copy(boxes = boxes)
    }

    fun totalBoxLoaded(total: Int) = manager.update {
        copy(totalBox = total)
    }

    fun shareLiked(shareId: String) = manager.update {
        copy(shares = shares.map { share ->
            if (share.shareId == shareId) {
                share.copy(likeNumber = share.likeNumber + 1, liked = true)
            } else {
                share
            }
        })
    }

    fun chooseBoxFor(value: HomeState.ChooseBoxFor?) = manager.update {
        copy(chooseBoxFor = value)
    }

    fun boxUpdated(boxDetail: BoxDetail) = manager.update {
        copy(boxes = boxes.map { box ->
            if (box.boxId == boxDetail.boxId) boxDetail else box
        })
    }
}
