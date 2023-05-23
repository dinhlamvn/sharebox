package com.dinhlam.sharebox.data.local.dao

import androidx.room.*
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.ShareMode

@Dao
interface ShareDao {

    @Insert
    suspend fun insertAll(vararg shares: Share)

    @Query("SELECT * FROM share ORDER BY id DESC")
    suspend fun find(): List<Share>

    @Query("SELECT * FROM share WHERE share_id = :shareId")
    suspend fun findOne(shareId: String): Share?

    @Query("SELECT * FROM share WHERE share_user_id = :shareUserId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun find(shareUserId: String, limit: Int, offset: Int): List<Share>

    @Query("SELECT * FROM share WHERE share_mode = :shareMode ORDER BY id DESC")
    suspend fun find(shareMode: ShareMode): List<Share>

    @Query("SELECT * FROM share WHERE share_mode = :shareMode ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun find(shareMode: ShareMode, limit: Int, offset: Int): List<Share>

    @Query("SELECT * FROM share WHERE share_id IN(:shareIds)")
    suspend fun find(shareIds: List<String>): List<Share>

    @Query(
        """
        SELECT s.* 
        FROM share as s
        INNER JOIN share_community sc ON sc.share_id = s.share_id
        ORDER BY sc.share_power DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    suspend fun findShareCommunity(limit: Int, offset: Int): List<Share>
}
