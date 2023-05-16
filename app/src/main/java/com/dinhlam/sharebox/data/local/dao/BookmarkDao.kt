package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.dinhlam.sharebox.data.local.entity.Bookmark

@Dao
interface BookmarkDao {

    @Insert
    fun bookmark(bookmark: Bookmark)
}