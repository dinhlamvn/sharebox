package com.dinhlam.sharebox.database.dao

import androidx.room.*
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.ShareMode

@Dao
interface ShareDao {

    @Insert
    fun insertAll(vararg shares: Share)

    @Query("SELECT * FROM share ORDER BY id DESC")
    fun find(): List<Share>

    @Query("SELECT * FROM share WHERE share_mode = :shareMode ORDER BY id DESC")
    fun find(shareMode: ShareMode): List<Share>
}
