package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.Bookmark

@Dao
interface BookmarkDao {

    @Insert
    fun insert(vararg bookmarks: Bookmark)

    @Upsert
    fun upsert(bookmark: Bookmark)

    @Query("DELETE FROM bookmark WHERE share_id = :shareId")
    fun delete(shareId: String)

    @Query("DELETE FROM bookmark WHERE share_id = :shareId AND bookmark_collection_id = :bookmarkCollectionId")
    fun delete(shareId: String, bookmarkCollectionId: String)

    @Query("SELECT * FROM bookmark WHERE share_id = :shareId")
    fun find(shareId: String): List<Bookmark>
}