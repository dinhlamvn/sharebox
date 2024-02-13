package com.dinhlam.sharebox.ui.boxdetail

import androidx.annotation.UiThread
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BoxDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val bookmarkRepository: BookmarkRepository,
) : BaseViewModel<BoxDetailState>(BoxDetailState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID))) {

    init {
        consume(BoxDetailState::boxId) { boxId ->
            boxRepository.updateLastSeen(boxId)
            loadShares(boxId)

        }
        loadBoxDetail()
    }

    private fun loadBoxDetail() = getState { state ->
        suspend {
            boxRepository.findOne(state.boxId)
        }.execute { boxDetail ->
            copy(boxDetail = boxDetail)
        }
    }

    private fun loadShares(boxId: String) {
        suspend {
            loadShares(boxId, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
        }.execute { shares ->
            copy(shares = shares, isRefreshing = false, isLoadingMore = false)
        }
    }

    fun loadMores() = getState { state ->
        setState { copy(isLoadingMore = true) }
        suspend {
            loadShares(
                state.boxId,
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { loadedShares ->
            copy(
                shares = this.shares.plus(loadedShares),
                isLoadingMore = false,
                canLoadMore = loadedShares.isNotEmpty(),
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

    fun doOnRefresh() = getState { state ->
        setState { BoxDetailState(boxId = state.boxId) }
        loadBoxDetail()
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

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
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
}