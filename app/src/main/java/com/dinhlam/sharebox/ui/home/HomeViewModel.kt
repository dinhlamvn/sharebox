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

    init {
        getListBoxes()
        getRecentlyShares()
    }

    private fun getListBoxes() = suspend {
        boxRepository.findByUser(userHelper.getCurrentUserId(), 10, 0)
    }.execute { boxes ->
        copy(boxes = boxes)
    }

    private fun getRecentlyShares() {
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                0
            )
        }.execute { shares ->
            copy(shares = shares, isRefreshing = false)
        }
    }

    fun loadMores() = getState { state ->
        setState { copy(isLoadingMore = true) }
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { loadShares ->
            copy(
                shares = this.shares.plus(loadShares),
                isLoadingMore = false,
                canLoadMore = loadShares.isNotEmpty(),
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
        getListBoxes()
        getRecentlyShares()
    }
}