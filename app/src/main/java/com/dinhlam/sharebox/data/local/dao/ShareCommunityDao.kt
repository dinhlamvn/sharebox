package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.ShareCommunity
import com.dinhlam.sharebox.data.model.ShareMode

@Dao
interface ShareCommunityDao {

    @Insert
    suspend fun insert(vararg shareCommunities: ShareCommunity)

    @Upsert
    suspend fun upsert(shareCommunity: ShareCommunity)

    @Query("SELECT * FROM share_community ORDER BY share_power DESC")
    suspend fun find(): List<ShareCommunity>

    @Query("SELECT * FROM share_community WHERE share_id = :shareId")
    suspend fun find(shareId: String): ShareCommunity?

    @Query(
        """
        SELECT sc.id, sc.share_id, sc.share_power, sc.created_at, sc.updated_at
        FROM share_community as sc
        JOIN share as s ON s.share_id = sc.share_id
        WHERE s.share_user_id = :shareUserId AND s.share_mode = :shareMode
        ORDER BY sc.share_power DESC
    """
    )
    suspend fun find(shareUserId: String, shareMode: ShareMode): List<ShareCommunity>
}