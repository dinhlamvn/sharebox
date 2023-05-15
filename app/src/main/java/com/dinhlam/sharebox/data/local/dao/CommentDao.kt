package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Comment

@Dao
interface CommentDao {

    @Insert
    fun insert(vararg comments: Comment)

    @Query("SELECT * FROM comment WHERE share_id = :shareId ORDER BY id DESC")
    fun find(shareId: String): List<Comment>
}