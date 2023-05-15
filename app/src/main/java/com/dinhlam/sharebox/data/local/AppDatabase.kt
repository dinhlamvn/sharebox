package com.dinhlam.sharebox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.converter.ShareModeConverter
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.StarDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.dao.VoteDao
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Folder
import com.dinhlam.sharebox.data.local.entity.HashTag
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.ShareHashTag
import com.dinhlam.sharebox.data.local.entity.Star
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.local.entity.Vote

@Database(
    entities = [Share::class, Folder::class, HashTag::class, User::class, Vote::class, ShareHashTag::class, Comment::class, Star::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ShareModeConverter::class, ShareDataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun userDao(): UserDao
    abstract fun voteDao(): VoteDao
    abstract fun commentDao(): CommentDao
    abstract fun starDao(): StarDao
}
