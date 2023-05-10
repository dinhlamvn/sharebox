package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.helper.ShareModelViewHelper
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.ui.home.profile.ProfileState
import com.dinhlam.sharebox.ui.share.ShareState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareModelViewHelper: ShareModelViewHelper
) : BaseViewModel<CommunityState>(CommunityState()) {

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
            delay(3000)
            setState { copy(shareList = shareList.plus(shares), isLoadMore = false) }
        }
    }

    fun doOnRefresh() {
        setState { CommunityState() }
        loadShares()
    }
}