package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharebox.data.local.entity.Like

@Dao
interface LikeDao {

    @Insert
    suspend fun insert(vararg like: Like)

    @Update
    suspend fun update(like: Like)

    @Query("SELECT COUNT(*) FROM `like` WHERE share_id = :shareId")
    suspend fun countByShare(shareId: String): Int

    @Query("SELECT * FROM `like` WHERE share_id = :shareId AND user_id = :userId")
    suspend fun find(shareId: String, userId: String): Like?

    @Query("SELECT * FROM `like` WHERE like_id = :likeId")
    suspend fun find(likeId: String): Like?

    @Query("""
        SELECT COUNT(*) 
        FROM `like` as l
        INNER JOIN share as s ON s.share_id = l.share_id
        WHERE s.share_user_id = :userId
    """)
    suspend fun countByUserShare(userId: String): Int
}