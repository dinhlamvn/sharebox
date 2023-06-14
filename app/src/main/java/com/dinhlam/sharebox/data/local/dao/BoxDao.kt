package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharebox.data.local.entity.Box

@Dao
interface BoxDao {

    @Insert
    suspend fun insert(vararg box: Box)

    @Update
    suspend fun update(vararg box: Box)

    @Query("SELECT * FROM `box` WHERE box_id = :boxId")
    suspend fun find(boxId: String): Box?

    @Query("SELECT * FROM `box` WHERE box_name LIKE '%' || :query || '%' ORDER BY box_name ASC")
    suspend fun search(query: String): List<Box>

    @Query("SELECT * FROM `box` ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
    suspend fun find(limit: Int, offset: Int): List<Box>

    @Query("SELECT * FROM `box` ORDER BY last_seen DESC LIMIT 6")
    suspend fun findLatestBoxes(): List<Box>

    @Query("SELECT COUNT(*) FROM `box`")
    suspend fun count(): Int
}