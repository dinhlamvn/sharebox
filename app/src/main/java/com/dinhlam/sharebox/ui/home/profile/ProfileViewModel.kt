package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareModelViewHelper
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.repository.UserRepository
import com.dinhlam.sharebox.ui.home.community.CommunityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareModelViewHelper: ShareModelViewHelper,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        getActiveUserInfo()
        consume(CommunityState::shares, true) { shares ->
            getState { state ->
                val nonNullUser = state.activeUser ?: return@getState
                setState {
                    copy(
                        shareModelViews = shareModelViewHelper.buildShareModelViews(
                            shares, mapOf(nonNullUser.userId to nonNullUser)
                        )
                    )
                }
            }
        }
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
            val shares = shareRepository.findAll()
            delay(1000)
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val others = shareRepository.findAll()
            delay(1000)
            setState { copy(shares = shares.plus(others), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { ProfileState() }
        getActiveUserInfo()
        loadShares()
    }
}