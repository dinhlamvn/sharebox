package com.dinhlam.sharebox.ui.home.community

import android.util.Log
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.pref.UserSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val voteRepository: VoteRepository,
    private val userSharePref: UserSharePref,
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
        setState { copy(bookmarkedShareIdSet = emptySet(), voteMap = emptyMap()) }
        loadShares()
    }

    fun vote(shareId: String) = backgroundTask {
        val result = voteRepository.vote(shareId, userSharePref.getActiveUserId())
        if (result) {
            syncVote(shareId)
        }
    }

    private suspend fun syncVote(shareId: String) {
        val voteCount = voteRepository.countVote(shareId)
        setState { copy(voteMap = voteMap.plus(shareId to voteCount)) }
    }

    fun bookmark(shareId: String, bookmarkCollectionId: String?) = backgroundTask {
        bookmarkCollectionId?.let { id ->
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            if (bookmarkDetail?.bookmarkCollectionId != bookmarkCollectionId) {
                val bookmarked =
                    bookmarkRepository.bookmark(bookmarkDetail?.id.orElse(0), shareId, id)
                if (bookmarked) {
                    setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.plus(shareId)) }
                }
            }
        } ?: run {
            val deleted = bookmarkRepository.delete(shareId)
            if (deleted) {
                setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.minus(shareId)) }
            }
        }
    }
}