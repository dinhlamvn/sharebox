package com.dinhlam.sharebox.ui.home.community

import android.util.Log
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.extensions.filterValuesNotNull
import com.dinhlam.sharebox.helper.ShareModelViewHelper
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareModelViewHelper: ShareModelViewHelper,
    private val userRepository: UserRepository,
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        consume(CommunityState::shares, true) { shares ->
            sync(shares)
        }
        loadShares()
    }

    private fun sync(shares: List<ShareDetail>) = execute { state ->
        val newMap = state.userMap.plus(shares.associate { share ->
            val user = state.userMap[share.userId] ?: userRepository.findOne(share.userId)
            share.userId to user
        }.filterValuesNotNull())
        setState {
            copy(
                shareModelViews = shareModelViewHelper.buildShareModelViews(shares, newMap),
                userMap = newMap
            )
        }
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask(onError = {
            Log.e("DinhLam", it.toString())
        }) {
            val shares = shareRepository.findAll(shareMode = ShareMode.ShareModeCommunity)
            delay(1000)
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val shares = shareRepository.findAll(shareMode = ShareMode.ShareModeCommunity)
            delay(3000)
            setState { copy(shares = this.shares.plus(shares), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { CommunityState() }
        loadShares()
    }
}