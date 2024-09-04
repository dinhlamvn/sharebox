package com.dinhlam.sharebox.ui.trash

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
) : BaseViewModel<TrashState>(TrashState()) {

    init {
        loadShares()
    }

    private fun loadShares() {
        suspend {
            loadShares(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
        }.execute { asyncLoad ->
            copy(shares = asyncLoad.data.orEmpty(), isRefreshing = asyncLoad is AsyncLoad.Loading)
        }
    }

    fun loadMores() = getState { state ->
        suspend {
            loadShares(
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
        limit: Int,
        offset: Int
    ): List<ShareDetail> {
        return shareRepository.findShareInTrash(
            limit,
            offset
        )
    }

    fun doOnRefresh() {
        setState { TrashState() }
        loadShares()
    }

    fun setCurrentShare(shareDetail: ShareDetail?) = setState { copy(currentShare = shareDetail) }

    fun moveShareToBox(boxId: String) = getState { state ->
        val currentShare = state.currentShare ?: return@getState
        val shareId = currentShare.shareId
        suspend {
            val share = shareRepository.findOneRaw(shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareBoxId = boxId))
                shareRepository.findOne(shareId)
            } ?: currentShare
        }.execute { asyncLoad ->
            if (asyncLoad.success) {
                copy(
                    shares = shares.filterNot { share -> share.shareId == shareId },
                    currentShare = null
                )
            } else {
                copy(currentShare = null)
            }
        }
    }
}