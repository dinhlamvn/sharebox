package com.dinhlam.sharebox.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinhlam.sharebox.database.dao.FolderDao
import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.database.entity.Share

@Database(
    entities = [Share::class, Folder::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun folderDao(): FolderDao
}
