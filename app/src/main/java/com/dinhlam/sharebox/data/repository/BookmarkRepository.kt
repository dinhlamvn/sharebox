package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.data.model.BookmarkDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
) {

    fun bookmark(id: Int, shareId: String, bookmarkCollectionId: String): Boolean {
        val bookmark = Bookmark(id, shareId = shareId, bookmarkCollectionId = bookmarkCollectionId)
        return bookmarkDao.runCatching {
            upsert(bookmark)
            true
        }.getOrDefault(false)
    }

    fun delete(shareId: String): Boolean {
        return bookmarkDao.runCatching {
            delete(shareId)
            true
        }.getOrDefault(false)
    }

    fun findOne(shareId: String): BookmarkDetail? = bookmarkDao.runCatching {
        find(shareId)?.let { bookmark ->
            BookmarkDetail(
                bookmark.id,
                bookmark.shareId,
                bookmark.bookmarkCollectionId
            )
        }
    }.getOrDefault(null)
}