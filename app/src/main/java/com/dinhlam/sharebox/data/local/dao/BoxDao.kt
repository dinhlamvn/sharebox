package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Box

@Dao
interface BoxDao {

    @Insert
    suspend fun insert(vararg box: Box)

    @Query("SELECT * FROM `box` WHERE box_id = :boxId")
    suspend fun find(boxId: String): Box?
}