package com.dinhlam.sharebox.ui.bookmark.list

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
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

    private fun loadBookmarkCollectionDetail() = execute {
        val bookmarkCollection = bookmarkCollectionRepository.find(bookmarkCollectionId)
        val passcode = bookmarkCollection?.passcode ?: ""
        copy(
            bookmarkCollection = bookmarkCollection,
            requestVerifyPasscode = passcode.isNotEmpty()
        )
    }

    private fun loadShares() = getState { state ->
        setState { copy(isSharesLoading = true) }
        val collectionId = state.bookmarkCollectionId
        doInBackground {
            val bookmarks = bookmarkRepository.find(collectionId)
            val ids = bookmarks.map { bookmarkDetail -> bookmarkDetail.shareId }
            val shares = shareRepository.find(ids)
            setState { copy(shares = shares, isSharesLoading = false) }
        }
    }

    fun markPasscodeVerified() = setState {
        copy(requestVerifyPasscode = false)
    }

    fun removeBookmark(shareId: String) = doInBackground {
        val deleted = bookmarkRepository.delete(shareId)
        if (deleted) {
            setState { copy(shares = shares.filterNot { shareDetail -> shareDetail.shareId == shareId }) }
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
}