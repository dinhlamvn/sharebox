package com.dinhlam.sharebox.ui.home.profile

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.helper.ShareModelViewHelper
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.ui.home.community.CommunityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareModelViewHelper: ShareModelViewHelper
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        consume(CommunityState::shareList, true) { shares ->
            setState { copy(shareModelViews = shareModelViewHelper.buildShareModelViews(shares)) }
        }
        loadShares()
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask {
            val shares = shareRepository.findAll(sortType = SortType.NEWEST)
            delay(1000)
            setState { copy(shareList = shares, isRefreshing = false) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadMore = true) }
        backgroundTask {
            val shares = shareRepository.findAll(sortType = SortType.NEWEST)
            delay(1000)
            setState { copy(shareList = shareList.plus(shares), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { ProfileState() }
        loadShares()
    }
}