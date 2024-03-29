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

    @Query("SELECT * FROM `box` WHERE created_by = :userId ORDER BY created_at DESC LIMIT 1")
    suspend fun findFirst(userId: String): Box?

    @Query("SELECT * FROM `box` WHERE (created_by = :userId AND box_name LIKE '%' || :query || '%') OR box_id = :query ORDER BY box_name ASC")
    suspend fun search(query: String, userId: String): List<Box>

    @Query("SELECT * FROM `box` ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
    suspend fun find(limit: Int, offset: Int): List<Box>

    @Query("SELECT * FROM `box` WHERE created_by = :userId ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
    suspend fun find(userId: String, limit: Int, offset: Int): List<Box>

    @Query("SELECT * FROM `box` ORDER BY last_seen DESC LIMIT 6")
    suspend fun findLatestBoxes(): List<Box>

    @Query("SELECT * FROM `box` WHERE passcode IS NULL OR passcode = '' ORDER BY last_seen DESC LIMIT 6")
    suspend fun findLatestBoxesWithoutPasscode(): List<Box>

    @Query("SELECT COUNT(*) FROM `box`")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM `box` WHERE created_by = :userId")
    suspend fun count(userId: String): Int

    @Query("SELECT * FROM box WHERE synced = 0")
    suspend fun findForSyncToCloud(): List<Box>
}