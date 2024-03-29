package com.dinhlam.sharebox.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.User

@Database(
    entities = [Share::class, User::class, Like::class, Comment::class, BookmarkCollection::class, Bookmark::class, Box::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = AppDatabase.Migration2To3::class),
    ]
)
@TypeConverters(ShareDataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun userDao(): UserDao
    abstract fun likeDao(): LikeDao
    abstract fun commentDao(): CommentDao
    abstract fun bookmarkCollectionDao(): BookmarkCollectionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun boxDao(): BoxDao

    @DeleteColumn(
        tableName = "video_mixer",
        columnName = "uri"
    )
    @DeleteColumn(
        tableName = "video_mixer",
        columnName = "trending_score"
    )
    class Migration2To3 : AutoMigrationSpec
}
