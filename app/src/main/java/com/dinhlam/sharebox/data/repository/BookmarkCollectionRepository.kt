package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.utils.BookmarkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkCollectionRepository @Inject constructor(
    private val bookmarkCollectionDao: BookmarkCollectionDao
) {

    fun createCollection(name: String, desc: String, thumbnail: String, passcode: String): Boolean {
        val collectionId = BookmarkUtils.createBookmarkCollectionId()
        val bookmarkCollection =
            BookmarkCollection(collectionId, name, thumbnail, desc, passcode.md5())
        return bookmarkCollectionDao.runCatching {
            insert(bookmarkCollection)
            true
        }.getOrDefault(false)
    }
}