package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
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
    BookmarkCollectionPickerState(
        savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID),
        savedStateHandle[AppExtras.EXTRA_BOOKMARK_COLLECTION_ID]
    )
) {


    init {
        loadBookmarkCollections()
    }

    private fun loadBookmarkCollections() = execute {
        val collections = bookmarkCollectionRepository.find()
        val bookmarkCollection = collectionId?.let { id -> bookmarkCollectionRepository.find(id) }
        copy(
            bookmarkCollections = collections,
            isLoading = false,
            pickedBookmarkCollection = bookmarkCollection,
            originalBookmarkCollection = bookmarkCollection,
        )
    }

    fun reloadAfterCreateNewBookmarkCollection() = doInBackground {
        val collections = bookmarkCollectionRepository.find()
        setState { copy(bookmarkCollections = collections) }
    }

    fun onPickBookmarkCollection(collection: BookmarkCollectionDetail) = getState { state ->
        if (state.pickedBookmarkCollection?.id == collection.id) {
            setState { copy(pickedBookmarkCollection = null) }
        } else {
            setState { copy(pickedBookmarkCollection = collection) }
        }
    }
}
