package com.dinhlam.sharebox.ui.home.trending

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
) : BaseViewModel<TrendingState>(TrendingState()) {

    init {
        loadTrendingShares()
    }

    private fun loadTrendingShares() {
        setState { copy(isRefreshing = true) }
        execute {
            val shares = loadShares(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
            copy(shares = shares, isRefreshing = false)
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val shares = loadShares(
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
            copy(
                shares = this.shares.plus(shares),
                isLoadingMore = false,
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1
            )
        }
    }

    private suspend fun loadShares(
        limit: Int, offset: Int
    ): List<ShareDetail> {
        return shareRepository.findTrendingShares(limit, offset)
    }

    fun doOnPullRefresh() {
        setState { TrendingState() }
        loadTrendingShares()
    }
}