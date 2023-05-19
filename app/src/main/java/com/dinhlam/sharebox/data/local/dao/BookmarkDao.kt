package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.Bookmark

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(vararg bookmarks: Bookmark)

    @Upsert
    suspend fun upsert(bookmark: Bookmark)

    @Query("DELETE FROM bookmark WHERE share_id = :shareId")
    suspend fun delete(shareId: String)

    @Query("DELETE FROM bookmark WHERE share_id = :shareId AND bookmark_collection_id = :bookmarkCollectionId")
    suspend fun delete(shareId: String, bookmarkCollectionId: String)

    @Query("SELECT * FROM bookmark WHERE share_id = :shareId")
    suspend fun find(shareId: String): Bookmark?

    @Query("SELECT * FROM bookmark WHERE bookmark_collection_id = :bookmarkCollectionId")
    suspend fun findAll(bookmarkCollectionId: String): List<Bookmark>
}