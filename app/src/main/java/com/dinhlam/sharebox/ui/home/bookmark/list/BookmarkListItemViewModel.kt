package com.dinhlam.sharebox.ui.home.bookmark.list

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.pref.UserSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkListItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val shareRepository: ShareRepository,
    private val voteRepository: VoteRepository,
    private val userSharePref: UserSharePref,
) : BaseViewModel<BookmarkListItemState>(BookmarkListItemState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID))) {

    init {
        loadBookmarkCollectionDetail()
        loadShares()
    }

    private fun loadBookmarkCollectionDetail() = execute { state ->
        val bookmarkCollection = bookmarkCollectionRepository.find(state.bookmarkCollectionId)
        val passcode = bookmarkCollection?.passcode ?: ""
        setState {
            copy(
                bookmarkCollection = bookmarkCollection,
                requestVerifyPasscode = passcode.isNotEmpty()
            )
        }
    }

    private fun loadShares() = getState { state ->
        setState { copy(isSharesLoading = true) }
        val collectionId = state.bookmarkCollectionId
        backgroundTask {
            val bookmarks = bookmarkRepository.find(collectionId)
            val ids = bookmarks.map { bookmarkDetail -> bookmarkDetail.shareId }
            val shares = shareRepository.find(ids)
            setState { copy(shares = shares, isSharesLoading = false) }
        }
    }

    fun markPasscodeVerified() = setState {
        copy(requestVerifyPasscode = false)
    }

    fun removeBookmark(shareId: String) = backgroundTask {
        val deleted = bookmarkRepository.delete(shareId)
        if (deleted) {
            setState { copy(shares = shares.filterNot { shareDetail -> shareDetail.shareId == shareId }) }
        }
    }

    fun vote(shareId: String) = backgroundTask {
        val result = voteRepository.vote(shareId, userSharePref.getActiveUserId())
        if (result) {
            setState {
                val shareList = shares.map { shareDetail ->
                    if (shareDetail.shareId == shareId) {
                        shareDetail.copy(voteCount = shareDetail.voteCount + 1)
                    } else {
                        shareDetail
                    }
                }
                copy(shares = shareList)
            }
        }
    }
}