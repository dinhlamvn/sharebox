package com.dinhlam.sharebox.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dinhlam.sharebox.database.converter.ShareDataConverter
import com.dinhlam.sharebox.database.converter.ShareModeConverter
import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.dao.UserDao
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.database.entity.HashTag
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.database.entity.ShareHashTag
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.database.entity.Vote

@Database(
    entities = [Share::class, Folder::class, HashTag::class, User::class, Vote::class, ShareHashTag::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ShareModeConverter::class, ShareDataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun userDao(): UserDao
}
