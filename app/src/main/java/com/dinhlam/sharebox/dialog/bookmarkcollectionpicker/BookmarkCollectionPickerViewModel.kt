package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.extensions.getNonNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkCollectionPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
    private val bookmarkRepository: BookmarkRepository,
) : BaseViewModel<BookmarkCollectionPickerState>(
    BookmarkCollectionPickerState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID))
) {


    init {
        loadBookmarkCollections()
    }

    private fun loadBookmarkCollections() = execute { state ->
        val collections = bookmarkCollectionRepository.find()
        val pickedId = bookmarkRepository.findOne(state.shareId)?.bookmarkCollectionId
        setState {
            copy(
                bookmarkCollections = collections,
                isLoading = false,
                pickedBookmarkCollectionId = pickedId,
                originalPickedBookmarkCollectionId = pickedId
            )
        }
    }

    fun reloadAfterCreateNewBookmarkCollection() = backgroundTask {
        val collections = bookmarkCollectionRepository.find()
        setState { copy(bookmarkCollections = collections) }
    }

    fun onPickBookmarkCollection(id: String) = getState { state ->
        if (state.pickedBookmarkCollectionId == id) {
            setState { copy(pickedBookmarkCollectionId = null) }
        } else {
            setState { copy(pickedBookmarkCollectionId = id) }
        }
    }
}
