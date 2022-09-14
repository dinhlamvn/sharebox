package com.dinhlam.sharekeeper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinhlam.sharekeeper.database.dao.ShareDao
import com.dinhlam.sharekeeper.database.entity.Share

@Database(entities = [Share::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
}