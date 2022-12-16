package com.dinhlam.sharebox.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dinhlam.sharebox.database.entity.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder")
    fun getAll(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg folders: Folder)

    @Update
    fun update(folder: Folder): Int

    @Delete
    fun delete(folder: Folder): Int

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getById(id: String): Folder

    @Query("SELECT * FROM folder WHERE tag = :tagId")
    fun getByTag(tagId: Int): List<Folder>
}
