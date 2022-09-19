package com.dinhlam.sharesaver.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharesaver.database.entity.Share

@Dao
interface ShareDao {
    @Query("SELECT * FROM share")
    fun getAll(): List<Share>

    @Query("SELECT * FROM share WHERE share_type = :shareType ORDER BY id DESC")
    fun getByShareType(shareType: String): List<Share>

    @Insert
    fun insertAll(vararg shares: Share)
}