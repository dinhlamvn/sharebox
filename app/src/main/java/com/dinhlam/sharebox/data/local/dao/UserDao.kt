package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.User

@Dao
interface UserDao {

    @Insert
    fun insert(vararg users: User)

    @Query("SELECT * FROM user WHERE user_id = :userId")
    fun findOne(userId: String): User?
}