package com.dinhlam.sharebox.ui.home.bookmark

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
) : BaseViewModel<BookmarkState>(BookmarkState()) {

    init {
        loadBookmarkCollections()
    }

    private fun loadBookmarkCollections() = backgroundTask {
        setState { copy(isRefreshing = true) }
        val bookmarkCollections = bookmarkCollectionRepository.find()
        setState { copy(bookmarkCollections = bookmarkCollections, isRefreshing = false) }
    }

    fun loadMores() {

    }

    fun doOnRefresh() {
        setState { BookmarkState() }
        loadBookmarkCollections()
    }
}