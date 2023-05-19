package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.data.mapper.BookmarkToBookmarkDetailMapper
import com.dinhlam.sharebox.data.model.BookmarkDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val mapper: BookmarkToBookmarkDetailMapper,
) {
    suspend fun bookmark(id: Int, shareId: String, bookmarkCollectionId: String): Boolean {
        val bookmark = Bookmark(id, shareId = shareId, bookmarkCollectionId = bookmarkCollectionId)
        return bookmarkDao.runCatching {
            upsert(bookmark)
            true
        }.getOrDefault(false)
    }

    suspend fun delete(shareId: String): Boolean {
        return bookmarkDao.runCatching {
            delete(shareId)
            true
        }.getOrDefault(false)
    }

    suspend fun findOne(shareId: String): BookmarkDetail? = bookmarkDao.runCatching {
        find(shareId)?.let { bookmark -> mapper.map(bookmark) }
    }.getOrDefault(null)

    suspend fun find(bookmarkCollectionId: String): List<BookmarkDetail> = bookmarkDao.runCatching {
        findAll(bookmarkCollectionId).map { bookmark -> mapper.map(bookmark) }
    }.getOrDefault(emptyList())
}