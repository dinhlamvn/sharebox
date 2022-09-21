package com.dinhlam.sharesaver.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharesaver.database.entity.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder")
    fun getAll(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg folders: Folder)

    @Update
    fun update(folder: Folder): Int

    @Delete
    fun delete(folder: Folder): Int

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getById(id: String): Folder
}