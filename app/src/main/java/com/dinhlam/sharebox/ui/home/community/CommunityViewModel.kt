package com.dinhlam.sharebox.ui.home.community

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        loadShares()
    }

    private fun loadShares() = getState { state ->
        setState { copy(isRefreshing = true) }
        backgroundTask {
            val shares = if (state.currentBox !is Box.All) {
                shareRepository.find(
                    state.currentBox, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, offset = 0
                )
            } else {
                shareRepository.find(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, offset = 0)
            }

            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val shares = if (currentBox !is Box.All) {
                shareRepository.find(
                    currentBox,
                    AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                    currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
                )
            } else {
                shareRepository.find(
                    AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                    currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
                )
            }
            copy(
                shares = this.shares.plus(shares),
                isLoadingMore = false,
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1
            )
        }
    }

    fun doOnRefresh() {
        setState { copy(currentPage = 1, isLoadingMore = false, canLoadMore = true) }
        loadShares()
    }

    fun like(shareId: String) = backgroundTask {
        val result = likeRepository.likeAndSyncToCloud(shareId, userHelper.getCurrentUserId())
        if (result) {
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
    }

    fun bookmark(shareId: String, bookmarkCollectionId: String?) = backgroundTask {
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
        backgroundTask {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun selectShareBox(shareBox: Box) = getState { state ->
        if (state.currentBox != shareBox) {
            setState { copy(currentBox = shareBox) }
            doOnRefresh()
        }
    }
}