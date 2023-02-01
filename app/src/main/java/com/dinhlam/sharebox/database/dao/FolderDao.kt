package com.dinhlam.sharebox.database.dao

import androidx.room.*
import com.dinhlam.sharebox.database.entity.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder")
    fun getAll(): List<Folder>

    @Query("SELECT * FROM folder ORDER BY created_at DESC")
    fun getAllNewest(): List<Folder>

    @Query("SELECT * FROM folder ORDER BY created_at ASC")
    fun getAllOldest(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg folders: Folder)

    @Update
    fun update(folder: Folder): Int

    @Delete
    fun delete(folder: Folder): Int

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getById(id: String): Folder?

    @Query("SELECT * FROM folder WHERE tag = :tagId")
    fun getByTag(tagId: Int): List<Folder>

    @Query("SELECT COUNT(*) FROM folder")
    fun count(): Int
}
