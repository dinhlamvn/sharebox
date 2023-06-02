package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Like

@Dao
interface LikeDao {

    @Insert
    suspend fun insert(vararg like: Like)

    @Query("SELECT COUNT(*) FROM `like` WHERE share_id = :shareId")
    suspend fun countByShare(shareId: String): Int

    @Query("SELECT * FROM `like` WHERE share_id = :shareId AND user_id = :userId")
    suspend fun find(shareId: String, userId: String): Like?

    @Query("SELECT * FROM `like` WHERE like_id = :likeId")
    suspend fun find(likeId: String): Like?
}