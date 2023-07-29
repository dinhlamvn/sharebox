package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkCollectionToBookmarkCollectionDetailMapper @Inject constructor() {

    fun map(bookmarkCollection: BookmarkCollection, shareCount: Int): BookmarkCollectionDetail {
        return bookmarkCollection.run {
            BookmarkCollectionDetail(id, name, thumbnail, description, passcode, shareCount)
        }
    }
}