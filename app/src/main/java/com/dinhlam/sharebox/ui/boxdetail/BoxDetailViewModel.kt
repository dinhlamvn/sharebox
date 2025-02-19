package com.dinhlam.sharebox.ui.boxdetail

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
) : BaseViewModel<BoxDetailState>(BoxDetailState()) {

    init {
        onChange(BoxDetailState::boxDetail) { boxDetail ->
            if (boxDetail != null) {
                updateLastSeen(boxDetail.boxId)
            }
        }
        loadBoxDetail(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID))
    }

    private fun updateLastSeen(boxId: String) = doInBackground {
        val box = boxRepository.findOneRaw(boxId) ?: return@doInBackground
        boxRepository.update(box.copy(lastSeen = nowUTCTimeInMillis()))
    }

    private fun loadBoxDetail(boxId: String) {
        suspend {
            boxRepository.findOne(boxId)!!
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxDetail = asyncLoad,
                boxDetail = asyncLoad.data
            )
        }
    }

    fun loadShares() = getState { state ->
        suspend {
            loadShares(state.boxDetail!!.boxId, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
        }.execute { asyncLoad ->
            copy(
                shares = asyncLoad.data ?: shares,
                isRefreshing = asyncLoad is AsyncLoad.Loading,
                requirePasscode = false
            )
        }
    }

    fun loadMores() = getState { state ->
        suspend {
            loadShares(
                state.boxDetail!!.boxId,
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { asyncLoad ->
            val shares = asyncLoad.data.orEmpty()
            copy(
                asyncLoadLoadMoreShares = asyncLoad,
                shares = this.shares.plus(shares),
                canLoadMore = shares.isNotEmpty(),
                currentPage = if (asyncLoad is AsyncLoad.Success) currentPage + 1 else currentPage,
            )
        }
    }

    private suspend fun loadShares(
        boxId: String,
        limit: Int,
        offset: Int
    ): List<ShareDetail> {
        return shareRepository.findWhereInBox(
            boxId,
            limit,
            offset
        )
    }

    fun doOnRefresh() {
        setState {
            copy(currentPage = 1, canLoadMore = false)
        }
        loadShares()
    }

    fun like(shareId: String) = doInBackground {
        setState {
            val shareList = shares.map { shareDetail ->
                if (shareDetail.shareId == shareId) {
                    shareDetail.copy(likeNumber = shareDetail.likeNumber + 1, liked = true)
                } else {
                    shareDetail
                }
            }
            copy(shares = shareList)
        }
    }

    fun reloadBoxDetail(id: String) {
        suspend {
            boxRepository.findOne(id)!!
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxDetail = asyncLoad,
                boxDetail = asyncLoad.data
            )
        }
    }

    fun updateShare(data: ShareDetail) = getState { state ->
        val newShares = state.shares.map { shareDetail ->
            if (shareDetail.shareId == data.shareId) {
                data
            } else {
                shareDetail
            }
        }
        setState { copy(shares = newShares) }
    }

    fun refresh() = getState { state ->
        if (state.boxDetail == null || state.requirePasscode) {
            return@getState
        }
        suspend {
            loadShares(
                state.boxDetail.boxId,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                0
            )
        }.execute { asyncLoad ->
            copy(
                shares = asyncLoad.data ?: shares,
                isRefreshing = asyncLoad is AsyncLoad.Loading
            )
        }
    }
}