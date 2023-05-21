package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection

@Dao
interface BookmarkCollectionDao {

    @Insert
    suspend fun insert(vararg bookmarkCollections: BookmarkCollection)

    @Update
    suspend fun update(bookmarkCollection: BookmarkCollection)

    @Query("SELECT * FROM bookmark_collection ORDER BY id DESC")
    suspend fun find(): List<BookmarkCollection>

    @Query("SELECT * FROM bookmark_collection WHERE id = :id")
    suspend fun find(id: String): BookmarkCollection?
}