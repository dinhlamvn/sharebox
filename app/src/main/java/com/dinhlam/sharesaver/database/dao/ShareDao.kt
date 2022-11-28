package com.dinhlam.sharesaver.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharesaver.database.entity.Share

@Dao
interface ShareDao {
    @Query("SELECT * FROM share")
    fun getAll(): List<Share>

    @Query("SELECT * FROM share WHERE folder_id = :folderId ORDER BY id DESC")
    fun getByFolder(folderId: String): List<Share>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg shares: Share)

    @Update
    fun update(share: Share): Int

    @Query("SELECT * FROM share WHERE id = :id")
    fun getById(id: Int): Share

    @Query("SELECT COUNT(*) FROM share WHERE folder_id = :folderId")
    fun countByFolder(folderId: String): Int

    @Query("DELETE FROM share WHERE folder_id = :folderId")
    fun deleteByFolder(folderId: String)
}
