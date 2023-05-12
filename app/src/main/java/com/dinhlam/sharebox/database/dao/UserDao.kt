package com.dinhlam.sharebox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinhlam.sharebox.database.entity.User

@Dao
interface UserDao {

    @Insert
    fun insert(vararg users: User)

    @Query("SELECT * FROM user WHERE user_id = :userId")
    fun findOne(userId: String): User?
}