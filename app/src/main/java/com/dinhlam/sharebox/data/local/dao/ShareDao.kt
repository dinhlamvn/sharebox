package com.dinhlam.sharebox.data.local.dao

import androidx.room.*
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.ShareMode

@Dao
interface ShareDao {

    @Insert
    fun insertAll(vararg shares: Share)

    @Query("SELECT * FROM share ORDER BY id DESC")
    fun find(): List<Share>

    @Query("SELECT * FROM share WHERE share_user_id = :shareUserId ORDER BY id DESC")
    fun find(shareUserId: String): List<Share>

    @Query("SELECT * FROM share WHERE share_mode = :shareMode ORDER BY id DESC")
    fun find(shareMode: ShareMode): List<Share>

    @Query("SELECT * FROM share WHERE share_id IN(:shareIds) ORDER BY id DESC")
    fun find(shareIds: List<String>): List<Share>
}
