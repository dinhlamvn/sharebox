package com.dinhlam.sharebox.ui.boxdetail

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
) : BaseViewModel<BoxDetailState>(BoxDetailState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID))) {

    init {
        loadBoxDetail()
        loadShares()
    }

    private fun loadBoxDetail() = execute {
        val boxDetail = boxRepository.findOne(boxId) ?: return@execute this
        copy(boxDetail = boxDetail)
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        execute {
            val shares = loadShares(boxId, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
            copy(shares = shares, isRefreshing = false, isLoadingMore = false)
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val shares = loadShares(
                boxId,
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
            copy(
                shares = this.shares.plus(shares),
                isLoadingMore = false,
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1,
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
        setState { BoxDetailState(boxId, boxDetail) }
        loadShares()
    }
}