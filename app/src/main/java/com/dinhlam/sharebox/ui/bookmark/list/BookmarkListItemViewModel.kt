package com.dinhlam.sharebox.ui.bookmark.list

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkListItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val shareRepository: ShareRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : BaseViewModel<BookmarkListItemState>(BookmarkListItemState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID))) {

    init {
        loadBookmarkCollectionDetail()
        loadShares()
    }

    private fun loadBookmarkCollectionDetail() = getState { state ->
        suspend { bookmarkCollectionRepository.find(state.bookmarkCollectionId) }.execute { asyncLoad ->
            val bookmarkCollectionDetail = asyncLoad.data ?: return@execute this
            val passcode = bookmarkCollectionDetail.passcode ?: ""
            copy(
                bookmarkCollection = bookmarkCollectionDetail,
                requestVerifyPasscode = passcode.isNotEmpty()
            )
        }
    }

    private fun loadShares() = getState { state ->
        suspend {
            val collectionId = state.bookmarkCollectionId
            val bookmarks = bookmarkRepository.find(collectionId)
            val ids = bookmarks.map { bookmarkDetail -> bookmarkDetail.shareId }
            shareRepository.find(ids)
        }.execute { asyncLoad ->
            copy(
                shares = asyncLoad.data.orEmpty(),
                isSharesLoading = asyncLoad is AsyncLoad.Loading
            )
        }
    }

    fun markPasscodeVerified() = setState {
        copy(requestVerifyPasscode = false)
    }

    fun removeBookmark(shareId: String) = suspend {
        val share = shareRepository.findOne(shareId)!!
        bookmarkRepository.delete(share.shareId)
        share
    }.execute { asyncLoad ->
        if (asyncLoad.success) {
            copy(
                asyncLoadRemoveShare = asyncLoad,
                shares = shares.filterNot { shareDetail -> shareDetail.shareId == shareId })
        } else {
            copy(asyncLoadRemoveShare = asyncLoad)
        }
    }

    fun like(shareId: String) = doInBackground {
        val result =
            likeRepository.like(shareId, userHelper.getCurrentUserId()) ?: return@doInBackground
        realtimeDatabaseRepository.push(result)
        setState {
            val shareList = shares.map { shareDetail ->
                if (shareDetail.shareId == shareId) {
                    shareDetail.copy(likeNumber = shareDetail.likeNumber + 1)
                } else {
                    shareDetail
                }
            }
            copy(shares = shareList)
        }
    }

    fun refresh() {
        loadShares()
    }
}