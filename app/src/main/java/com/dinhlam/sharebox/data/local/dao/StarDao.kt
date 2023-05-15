package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Star

@Dao
interface StarDao {
    @Insert
    fun insert(vararg stars: Star)

    @Query("DELETE FROM star WHERE share_id = :shareId")
    fun delete(shareId: String)

    @Query("SELECT * from star WHERE share_id = :shareId")
    fun find(shareId: String): Star?

    @Query("SELECT * FROM star ORDER BY id DESC")
    fun find(): List<Star>
}