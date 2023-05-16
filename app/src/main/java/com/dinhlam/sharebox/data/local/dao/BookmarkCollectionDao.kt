package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection

@Dao
interface BookmarkCollectionDao {

    @Insert
    fun insert(vararg bookmarkCollections: BookmarkCollection)
}