package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.UserSharePref
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
        getActiveUserInfo()
        loadShares()
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
}