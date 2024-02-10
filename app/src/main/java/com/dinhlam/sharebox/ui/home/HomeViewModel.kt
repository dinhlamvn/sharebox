package com.dinhlam.sharebox.ui.home

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.pref.AppSharePref
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
    private val appSharePref: AppSharePref,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val videoHelper: VideoHelper,
) : BaseViewModel<HomeState>(HomeState(isRefreshing = true)) {

    init {
        getListBoxes()
        getCurrentActiveBox()
        consume(HomeState::currentBox) {
            getShares()
        }
    }

    private fun getListBoxes() = suspend {
        boxRepository.findByUser(userHelper.getCurrentUserId(), 1000, 0)
    }.execute { boxes ->
        copy(boxes = boxes)
    }

    private fun getCurrentActiveBox() = suspend {
        val boxId = appSharePref.getLatestActiveBoxId().takeIfNotNullOrBlank()
        boxId?.let { id -> boxRepository.findOne(id) }
    }.execute { box ->
        copy(currentBox = box, isRefreshing = false)
    }

    private fun getShares() {
        getState { state ->
            setState { copy(isRefreshing = true) }
            suspend {
                getShares(
                    state.currentBox, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0
                )
            }.execute { shares ->
                copy(shares = shares, isRefreshing = false, isLoadingMore = false)
            }
        }
    }

    fun loadMores() = getState { state ->
        suspend {
            getShares(
                state.currentBox,
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                state.currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
        }.execute { shares ->
            copy(
                shares = this.shares.plus(shares),
                isLoadingMore = false,
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1,
            )
        }
    }

    private suspend fun getShares(
        boxDetail: BoxDetail?, limit: Int, offset: Int
    ): List<ShareDetail> {
        return boxDetail?.let { box ->
            shareRepository.findWhereInBox(
                box.boxId, limit, offset
            )
        } ?: shareRepository.find(userHelper.getCurrentUserId(), limit, offset)
    }

    fun doOnRefresh() {
        setState { HomeState() }
        getListBoxes()
        getCurrentActiveBox()
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

    fun setBox(box: BoxDetail?) = getState { state ->
        if (state.currentBox != box) {
            setState { copy(currentBox = box) }
            box?.let { nonNullBox ->
                doInBackground {
                    boxRepository.updateLastSeen(nonNullBox.boxId, nowUTCTimeInMillis())
                }
                appSharePref.setLatestActiveBoxId(nonNullBox.boxId)
            } ?: appSharePref.setLatestActiveBoxId("")
        } else {
            appSharePref.setLatestActiveBoxId("")
            setState { copy(currentBox = null) }
        }
    }

    fun setBox(boxId: String) {
        doInBackground {
            val boxDetail = boxRepository.findOne(boxId) ?: return@doInBackground setBox(null)
            setBox(boxDetail)
        }
    }
}