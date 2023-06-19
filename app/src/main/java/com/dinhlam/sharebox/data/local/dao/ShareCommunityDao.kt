package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.ShareCommunity

@Dao
interface ShareCommunityDao {

    @Insert
    suspend fun insert(vararg shareCommunities: ShareCommunity)

    @Delete
    suspend fun delete(shareCommunity: ShareCommunity)

    @Upsert
    suspend fun upsert(shareCommunity: ShareCommunity)

    @Query("SELECT * FROM share_community ORDER BY share_power DESC")
    suspend fun find(): List<ShareCommunity>

    @Query("SELECT * FROM share_community WHERE share_id = :shareId")
    suspend fun find(shareId: String): ShareCommunity?

    @Query(
        """
        SELECT sc.* 
        FROM share_community as sc
        INNER JOIN share as s ON s.share_id = sc.share_id
        WHERE s.share_date < :timeToClean
        LIMIT 10
        """
    )
    suspend fun findShareToCleanUp(timeToClean: Long): List<ShareCommunity>
}