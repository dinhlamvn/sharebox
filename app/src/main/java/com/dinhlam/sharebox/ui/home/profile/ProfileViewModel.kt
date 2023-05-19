package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.ui.home.community.CommunityState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val voteRepository: VoteRepository,
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        consume(CommunityState::shares, true) { shares ->
            execute { state ->
                shares.forEach { share ->
                    if (state.shareVoteMap[share.shareId] == null) {
                        syncVote(share.shareId)
                    }
                    bookmarkRepository.findOne(share.shareId)?.let { bookmark ->
                        setState { copy(bookmarkedShareIdSet = bookmarkedShareIdSet.plus(bookmark.shareId)) }
                    }
                }
            }
        }

        getActiveUserInfo()
        loadShares()
    }

    private fun getActiveUserInfo() = backgroundTask {
        val activeUserId =
            userSharePref.getActiveUserId().takeIfNotNullOrBlank() ?: return@backgroundTask
        val user = userRepository.findOne(activeUserId) ?: return@backgroundTask
        setState { copy(activeUser = user) }
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask {
            val shares = shareRepository.find(userSharePref.getActiveUserId())
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val others = shareRepository.find(userSharePref.getActiveUserId())
            setState { copy(shares = shares.plus(others), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { ProfileState() }
        getActiveUserInfo()
        loadShares()
    }

    fun vote(shareId: String) = backgroundTask {
        val result = voteRepository.vote(shareId, userSharePref.getActiveUserId())
        if (result) {
            syncVote(shareId)
        }
    }

    private suspend fun syncVote(shareId: String) {
        val voteCount = voteRepository.count(shareId)
        setState { copy(shareVoteMap = shareVoteMap.plus(shareId to voteCount)) }
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