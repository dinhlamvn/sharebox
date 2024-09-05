package com.dinhlam.sharebox.ui.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<ProfileState>(ProfileState()) {

    init {
        getCurrentUserProfile()
    }

    fun getCurrentUserProfile() =
        suspend {
            val userDetail = userRepository.findOne(userHelper.getCurrentUserId())
            val shareCount = shareRepository.countByUser(userHelper.getCurrentUserId())
            userDetail to shareCount
        }.execute { asyncLoad ->
            val pair = asyncLoad.data
            copy(
                currentUser = pair?.first,
                shareCount = pair?.second ?: 0,
                isRefreshing = asyncLoad is AsyncLoad.Loading
            )
        }
}