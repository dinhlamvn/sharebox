package com.dinhlam.sharebox.ui.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        getCurrentUserProfile()
    }

    fun getCurrentUserProfile() = doInBackground {
        setState { copy(isRefreshing = true) }
        val user = userRepository.findOne(userHelper.getCurrentUserId()) ?: return@doInBackground
        setState { copy(currentUser = user, isRefreshing = false) }
    }
}