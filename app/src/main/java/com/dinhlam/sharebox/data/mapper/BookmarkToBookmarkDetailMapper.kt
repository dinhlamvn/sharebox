package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.model.BookmarkDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkToBookmarkDetailMapper @Inject constructor() {

    fun map(bookmark: Bookmark): BookmarkDetail = BookmarkDetail(
        bookmark.id, bookmark.shareId, bookmark.bookmarkCollectionId
    )
}