package com.dinhlam.sharebox.ui.home.community

import android.util.Log
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.StarRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.pref.UserSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val voteRepository: VoteRepository,
    private val userSharePref: UserSharePref,
    private val starRepository: StarRepository,
    private val bookmarkRepository: BookmarkRepository,
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        consume(CommunityState::shares, true) { shares ->
            execute { state ->
                shares.forEach { share ->
                    if (state.voteMap[share.shareId] == null) {
                        syncVote(share.shareId)
                    }
                    bookmarkRepository.findOne(share.shareId)?.let { bookmark ->
                        setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.plus(bookmark.shareId)) }
                    }
                }
            }
        }

        loadShares()
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask(onError = {
            Log.e("DinhLam", it.toString())
        }) {
            val shares = shareRepository.find(shareMode = ShareMode.ShareModeCommunity)
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val shares = shareRepository.find(shareMode = ShareMode.ShareModeCommunity)
            setState { copy(shares = this.shares.plus(shares), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { copy() }
        loadShares()
    }

    fun vote(shareId: String) = backgroundTask {
        val result = voteRepository.vote(shareId, userSharePref.getActiveUserId())
        if (result) {
            syncVote(shareId)
        }
    }

    private fun syncVote(shareId: String) {
        val voteCount = voteRepository.countVote(shareId)
        setState { copy(voteMap = voteMap.plus(shareId to voteCount)) }
    }

    fun starred(shareId: String) = backgroundTask {
        val star = starRepository.findOne(shareId) ?: return@backgroundTask run {
            val result = starRepository.starred(shareId)
            if (result) {
                setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.plus(shareId)) }
            }
        }
        val result = starRepository.unStarred(star.shareId)
        if (result) {
            setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.minus(star.shareId)) }
        }
    }

    fun bookmark(shareId: String, bookmarkCollectionIds: List<String>) = backgroundTask {
        if (bookmarkCollectionIds.isEmpty()) {
            val deleted = bookmarkRepository.delete(shareId)
            if (deleted) {
                setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.minus(shareId)) }
            }
        } else {
            val bookmarkIds = bookmarkRepository.find(shareId).map { it.bookmarkCollectionId }
            bookmarkIds.forEach { id ->
                if (!bookmarkCollectionIds.contains(id)) {
                    bookmarkRepository.delete(shareId, id)
                }
            }
            val newBookmarkIds = bookmarkCollectionIds.filterNot { id -> bookmarkIds.contains(id) }
            val set = mutableSetOf<String>()
            newBookmarkIds.forEach { id ->
                if (bookmarkRepository.bookmark(shareId, id)) {
                    set.add(id)
                }
            }
            setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.plus(set)) }
        }
    }
}