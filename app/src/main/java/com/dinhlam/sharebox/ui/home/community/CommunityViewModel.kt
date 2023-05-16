package com.dinhlam.sharebox.ui.home.community

import android.util.Log
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.ShareMode
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
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        consume(CommunityState::shares, true) { shares ->
            execute { state ->
                shares.forEach { share ->
                    if (state.voteMap[share.shareId] == null) {
                        syncVote(share.shareId)
                    }
                    starRepository.findOne(share.shareId)?.let {
                        setState { copy(starredSet = starredSet.plus(it.shareId)) }
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
                setState { copy(starredSet = starredSet.plus(shareId)) }
            }
        }
        val result = starRepository.unStarred(star.shareId)
        if (result) {
            setState { copy(starredSet = starredSet.minus(star.shareId)) }
        }
    }
}