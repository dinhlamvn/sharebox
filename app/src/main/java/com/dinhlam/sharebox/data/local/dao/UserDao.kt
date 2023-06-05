package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.User

@Dao
interface UserDao {

    @Insert
    suspend fun insert(vararg users: User)

    @Upsert
    suspend fun upsert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user WHERE user_id = :userId")
    suspend fun findOne(userId: String): User?
}