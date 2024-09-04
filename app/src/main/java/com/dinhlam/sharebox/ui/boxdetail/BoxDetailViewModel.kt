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
import com.dinhlam.sharebox.model.ShareData
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
        loadBoxDetail()
    }

    private fun loadBoxDetail() = getState { state ->
        suspend {
            boxRepository.findOne(state.boxId)!!
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxDetail = asyncLoad,
                boxDetail = asyncLoad.data,
                isRefreshing = false,
                mustInputPasscode = true
            )
        }
    }

    fun loadShares() = getState { state ->
        suspend {
            loadShares(state.boxId, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
        }.execute { asyncLoad ->
            copy(shares = asyncLoad.data.orEmpty(), isRefreshing = asyncLoad is AsyncLoad.Loading)
        }
    }

    fun loadMores() = getState { state ->
        suspend {
            loadShares(
                state.boxId,
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

    fun doOnRefresh() = getState { state ->
        setState { BoxDetailState(boxId = state.boxId, boxDetail = boxDetail) }
        loadShares()
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

    fun reloadBoxDetail(id: String) {
        suspend {
            boxRepository.findOne(id)!!
        }.execute { asyncLoad ->
            copy(
                asyncLoadBoxDetail = asyncLoad,
                boxDetail = asyncLoad.data,
                mustInputPasscode = false
            )
        }
    }

    fun setCurrentShare(shareDetail: ShareDetail?) = setState { copy(currentShare = shareDetail) }

    fun saveShareText(text: String?) = getState { state ->
        val currentShare = state.currentShare ?: return@getState
        val shareId = currentShare.shareId
        suspend {
            val share = shareRepository.findOneRaw(shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareData = ShareData.ShareText(text.orEmpty())))
                shareRepository.findOne(shareId)
            } ?: currentShare
        }.execute { asyncLoad -> copy(currentShare = null, asyncLoadSave = asyncLoad) }
    }

    fun saveShareNote(text: String?) = getState { state ->
        val currentShare = state.currentShare ?: return@getState
        val shareId = currentShare.shareId
        suspend {
            val share = shareRepository.findOneRaw(shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareNote = text))
                shareRepository.findOne(shareId)
            } ?: currentShare
        }.execute { asyncLoad -> copy(currentShare = null, asyncLoadSave = asyncLoad) }
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

    fun moveShareToBox(boxId: String) = getState { state ->
        val currentShare = state.currentShare ?: return@getState
        val shareId = currentShare.shareId
        suspend {
            val share = shareRepository.findOneRaw(shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareBoxId = boxId))
                shareRepository.findOne(shareId)
            } ?: currentShare
        }.execute { asyncLoad -> copy(currentShare = null, asyncLoadSave = asyncLoad) }
    }

    fun moveShareToTrash(shareId: String) {
        suspend {
            val share = shareRepository.findOneRaw(shareId)!!
            shareRepository.update(share.copy(shareBoxId = null))
            shareRepository.findOne(shareId)!!
        }.execute { asyncLoad ->
            if (asyncLoad.success) {
                copy(shares = shares.filterNot { share -> share.shareId == shareId })
            } else {
                this
            }
        }
    }
}