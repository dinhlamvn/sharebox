package com.dinhlam.sharebox.ui.bookmark

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

    private fun loadBookmarkCollections() {
        setState { copy(isRefreshing = true) }
        doInBackground {
            suspend { bookmarkCollectionRepository.find() }.execute { list ->
                copy(bookmarkCollections = list, isRefreshing = false)
            }
        }
    }

    fun doOnRefresh() {
        loadBookmarkCollections()
    }

    fun deleteBookmarkCollection(bookmarkCollectionId: String) {
        doInBackground {
            val result = bookmarkCollectionRepository.delete(bookmarkCollectionId)
            if (result) {
                loadBookmarkCollections()
            }
        }
    }
}