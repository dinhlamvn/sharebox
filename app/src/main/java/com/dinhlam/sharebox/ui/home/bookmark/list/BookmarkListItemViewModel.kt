package com.dinhlam.sharebox.ui.home.bookmark.list

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.extensions.getNonNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkListItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
) : BaseViewModel<BookmarkListItemState>(BookmarkListItemState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID))) {

    init {
        loadBookmarkCollectionDetail()
    }

    private fun loadBookmarkCollectionDetail() = execute { state ->
        val bookmarkCollection = bookmarkCollectionRepository.find(state.bookmarkCollectionId)
        val passcode = bookmarkCollection?.passcode ?: ""
        setState { copy(bookmarkCollection = bookmarkCollection, requestVerifyPasscode = passcode.isNotEmpty()) }
    }

    fun markPasscodeVerified() = setState {
        copy(requestVerifyPasscode = false)
    }
}