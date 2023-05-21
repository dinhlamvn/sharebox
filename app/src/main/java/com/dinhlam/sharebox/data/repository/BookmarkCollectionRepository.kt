package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.data.mapper.BookmarkCollectionToBookmarkCollectionDetailMapper
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkCollectionRepository @Inject constructor(
    private val bookmarkCollectionDao: BookmarkCollectionDao,
    private val bookmarkRepository: BookmarkRepository,
    private val mapper: BookmarkCollectionToBookmarkCollectionDetailMapper
) {
    suspend fun createCollection(
        collectionId: String, name: String, desc: String, thumbnail: String, passcode: String?
    ): Boolean {
        val passCode = passcode.takeIfNotNullOrBlank()
        val bookmarkCollection =
            BookmarkCollection(collectionId, name, thumbnail, desc, passCode?.md5())
        return bookmarkCollectionDao.runCatching {
            insert(bookmarkCollection)
            true
        }.onFailure {
            Logger.error(it.toString())
        }.getOrDefault(false)
    }

    suspend fun updateCollection(
        collectionId: String, block: BookmarkCollection.() -> BookmarkCollection
    ): Boolean {
        return bookmarkCollectionDao.runCatching {
            val bookmarkCollection = find(collectionId) ?: return false
            update(bookmarkCollection.run(block))
            true
        }.onFailure {
            Logger.error(it.toString())
        }.getOrDefault(false)
    }

    suspend fun find(): List<BookmarkCollectionDetail> = bookmarkCollectionDao.runCatching {
        val bookmarkCollections = find()
        bookmarkCollections.map { bookmarkCollection ->
            val shareCount = bookmarkRepository.count(bookmarkCollection.id)
            mapper.map(bookmarkCollection, shareCount)
        }
    }.getOrDefault(emptyList())

    suspend fun find(id: String): BookmarkCollectionDetail? = bookmarkCollectionDao.runCatching {
        val bookmarkCollection = find(id) ?: return@runCatching null
        val shareCount = bookmarkRepository.count(bookmarkCollection.id)
        mapper.map(bookmarkCollection, shareCount)
    }.getOrNull()
}