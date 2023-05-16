package com.dinhlam.sharebox.ui.home.bookmark

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.StarRepository
import com.dinhlam.sharebox.data.repository.VoteRepository
import com.dinhlam.sharebox.pref.UserSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val voteRepository: VoteRepository,
    private val userSharePref: UserSharePref,
    private val starRepository: StarRepository,
) : BaseViewModel<BookmarkState>(BookmarkState()) {

    init {

    }

    private fun loadShares() {

    }

    fun loadMores() {

    }

    fun doOnRefresh() {
        setState { BookmarkState() }
        loadShares()
    }
}