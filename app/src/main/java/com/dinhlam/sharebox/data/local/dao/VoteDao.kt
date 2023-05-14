package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.Vote

@Dao
interface VoteDao {

    @Insert
    fun insert(vararg vote: Vote)

    @Query("SELECT COUNT(*) FROM vote WHERE share_id = :shareId")
    fun countVote(shareId: String): Int
}