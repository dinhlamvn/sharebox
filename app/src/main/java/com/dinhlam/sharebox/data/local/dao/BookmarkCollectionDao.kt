package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection

@Dao
interface BookmarkCollectionDao {

    @Insert
    fun insert(vararg bookmarkCollections: BookmarkCollection)

    @Query("SELECT * FROM bookmark_collection ORDER BY id DESC")
    fun find(): List<BookmarkCollection>
}