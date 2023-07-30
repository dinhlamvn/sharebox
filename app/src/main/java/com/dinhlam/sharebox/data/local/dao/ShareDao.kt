package com.dinhlam.sharebox.data.local.dao

import androidx.room.*
import com.dinhlam.sharebox.data.local.entity.Share

@Dao
interface ShareDao {

    @Insert
    suspend fun insertAll(vararg shares: Share)

    @Update
    suspend fun update(share: Share)

    @Query("SELECT * FROM share ORDER BY id DESC")
    suspend fun find(): List<Share>

    @Query("SELECT * FROM share ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun find(limit: Int, offset: Int): List<Share>

    @Query("SELECT * FROM share WHERE share_id = :shareId")
    suspend fun findOne(shareId: String): Share?

    @Query("SELECT * FROM share WHERE share_user_id = :shareUserId ORDER BY share_date DESC LIMIT :limit OFFSET :offset")
    suspend fun find(shareUserId: String, limit: Int, offset: Int): List<Share>

    @Query(
        """
        SELECT s.* 
        FROM share as s 
        LEFT JOIN box as b ON b.box_id = s.share_box_id
        WHERE (s.share_box_id IS NULL OR (s.share_box_id NOT NULL AND b.passcode IS NULL))
        ORDER BY s.share_date DESC 
        LIMIT :limit 
        OFFSET :offset"""
    )
    suspend fun findForCommunity(limit: Int, offset: Int): List<Share>

    @Query("SELECT * FROM share WHERE share_id IN(:shareIds)")
    suspend fun find(shareIds: List<String>): List<Share>

    @Query(
        """
        SELECT s.* 
        FROM share as s
        WHERE s.share_box_id = :boxId
        ORDER BY s.share_date DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    suspend fun findWhereInBox(boxId: String, limit: Int, offset: Int): List<Share>

    @Query("SELECT COUNT(*) FROM share WHERE share_user_id = :userId")
    suspend fun countByUser(userId: String): Int

    @Query(
        """
        SELECT s.* 
        FROM share as s
        WHERE NOT EXISTS (SELECT 1 FROM video_mixer vm WHERE vm.share_id = s.share_id)
        AND s.is_video_share = 1 
        ORDER BY s.share_date DESC  
        LIMIT :limit
    """
    )
    suspend fun findForSyncVideos(
        limit: Int,
    ): List<Share>
}
