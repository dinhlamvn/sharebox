package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Comment

@Dao
interface CommentDao {

    @Insert
    suspend fun insert(vararg comments: Comment)

    @Query("SELECT * FROM comment WHERE share_id = :shareId ORDER BY id DESC")
    suspend fun find(shareId: String): List<Comment>

    @Query("SELECT * FROM comment WHERE comment_id = :commentId ORDER BY id DESC")
    suspend fun findOne(commentId: String): Comment?

    @Query("SELECT COUNT(*) FROM comment WHERE share_id = :shareId")
    suspend fun count(shareId: String): Int
}