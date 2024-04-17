package com.dinhlam.sharebox.ui.home

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
    private val boxRepository: BoxRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : BaseViewModel<HomeState>(HomeState(isRefreshing = true)) {

    companion object {
        private const val BOX_LIST_INIT_LOAD_SIZE_DEFAULT = 5
    }

    init {
        refresh()

        consume(HomeState::asyncLoadShares) { asyncLoad ->
            if (asyncLoad.completed) {
                triggerCanLoadMore()
            }
        }
    }

    private fun getTotalBoxes() = suspend { boxRepository.count(userHelper.getCurrentUserId()) }
        .execute { asyncLoad ->
            copy(totalBox = asyncLoad.data.orElse(0))
        }

    private fun triggerCanLoadMore() = getState { state ->
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { asyncLoad -> copy(canLoadMore = !asyncLoad.data.isNullOrEmpty()) }
    }

    private fun getListBoxes() = suspend {
        boxRepository.findByUser(userHelper.getCurrentUserId(), BOX_LIST_INIT_LOAD_SIZE_DEFAULT, 0)
    }.execute { asyncLoad ->
        copy(boxes = asyncLoad.data.orEmpty())
    }

    private fun getRecentlyShares() {
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                0
            )
        }.execute { asyncLoad ->
            copy(shares = asyncLoad.data.orEmpty(), isRefreshing = asyncLoad is AsyncLoad.Loading)
        }
    }

    fun loadMores() = getState { state ->
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { asyncLoad ->
            val shares = asyncLoad.data.orEmpty()
            copy(
                shares = this.shares.plus(shares),
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1,
            )
        }
    }

    fun like(shareId: String) = doInBackground {
        val result =
            likeRepository.like(shareId, userHelper.getCurrentUserId()) ?: return@doInBackground
        realtimeDatabaseRepository.push(result)
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

    fun bookmark(shareId: String, bookmarkCollectionId: String?) = doInBackground {
        bookmarkCollectionId?.let { id ->
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            if (bookmarkDetail?.bookmarkCollectionId != bookmarkCollectionId) {
                val bookmarked =
                    bookmarkRepository.bookmark(bookmarkDetail?.id.orElse(0), shareId, id)
                if (bookmarked) {
                    setState {
                        val shareList = shares.map { shareDetail ->
                            if (shareDetail.shareId == shareId) {
                                shareDetail.copy(bookmarked = true)
                            } else {
                                shareDetail
                            }
                        }
                        copy(shares = shareList)
                    }
                }
            }
        } ?: run {
            val deleted = bookmarkRepository.delete(shareId)
            if (deleted) {
                setState {
                    val shareList = shares.map { shareDetail ->
                        if (shareDetail.shareId == shareId) {
                            shareDetail.copy(bookmarked = false)
                        } else {
                            shareDetail
                        }
                    }
                    copy(shares = shareList)
                }
            }
        }
    }

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun doOnRefresh() {
        setState { HomeState(isRefreshing = true) }
        refresh()
    }

    private fun refresh() {
        getTotalBoxes()
        getListBoxes()
        getRecentlyShares()
    }
}