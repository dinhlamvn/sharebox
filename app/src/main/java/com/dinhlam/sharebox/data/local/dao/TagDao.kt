package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Tag

@Dao
interface TagDao {

    @Query("SELECT * FROM tag")
    suspend fun find(): List<Tag>

    @Query("SELECT * FROM tag WHERE id = :id")
    suspend fun find(id: Int): Tag?
}