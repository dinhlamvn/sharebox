package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
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
            val shares = shareRepository.findAll()
            setState { copy(shares = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val others = shareRepository.findAll()
            setState { copy(shares = shares.plus(others), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { ProfileState() }
        getActiveUserInfo()
        loadShares()
    }
}