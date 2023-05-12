package com.dinhlam.sharebox.ui.home.community

import android.util.Log
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val voteRepository: VoteRepository,
    private val userSharePref: UserSharePref
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        consume(CommunityState::shares, true) { shares ->
            execute { state ->
                shares.forEach { share ->
                    if (state.voteMap[share.shareId] == null) {
                        syncVote(share.shareId)
                    }
                }
            }
        }

        loadShares()
    }

    private fun syncVote(shareId: String) {
        val voteCount = voteRepository.countVote(shareId)
        setState { copy(voteMap = voteMap.plus(shareId to voteCount)) }
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask(onError = {
            Log.e("DinhLam", it.toString())
        }) {
            val shares = shareRepository.findAll(shareMode = ShareMode.ShareModeCommunity)
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val shares = shareRepository.findAll(shareMode = ShareMode.ShareModeCommunity)
            setState { copy(shares = this.shares.plus(shares), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { CommunityState() }
        loadShares()
    }

    fun vote(shareId: String) = backgroundTask {
        val result = voteRepository.vote(shareId, userSharePref.getActiveUserId())
        if (result) {
            syncVote(shareId)
        }
    }
}