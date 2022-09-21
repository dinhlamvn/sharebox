package com.dinhlam.sharesaver.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinhlam.sharesaver.database.dao.FolderDao
import com.dinhlam.sharesaver.database.dao.ShareDao
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.database.entity.Share

@Database(
    entities = [Share::class, Folder::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun folderDao(): FolderDao
}