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

    fun bookmark(shareId: String, bookmarkCollectionId: String): Boolean {
        val bookmark = Bookmark(shareId = shareId, bookmarkCollectionId = bookmarkCollectionId)
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

    fun delete(shareId: String, bookmarkCollectionId: String): Boolean {
        return bookmarkDao.runCatching {
            delete(shareId, bookmarkCollectionId)
            true
        }.getOrDefault(false)
    }

    fun findOne(shareId: String): BookmarkDetail? = bookmarkDao.runCatching {
        find(shareId).firstOrNull()?.let { bookmark ->
            BookmarkDetail(
                bookmark.shareId,
                bookmark.bookmarkCollectionId
            )
        }
    }.getOrDefault(null)

    fun find(shareId: String): List<BookmarkDetail> = bookmarkDao.runCatching {
        find(shareId).map { bookmark ->
            BookmarkDetail(
                bookmark.shareId,
                bookmark.bookmarkCollectionId
            )
        }
    }.getOrDefault(emptyList())
}