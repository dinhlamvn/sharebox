package com.dinhlam.sharebox.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.TagDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.Tag
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.extensions.insertDefaultTags

@Database(
    entities = [Share::class, User::class, Like::class, Comment::class, BookmarkCollection::class, Bookmark::class, Box::class, Tag::class],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = AppDatabase.Migration2To3::class),
        AutoMigration(from = 5, to = 6, spec = AppDatabase.Migration5To6::class),
        AutoMigration(from = 6, to = 7, spec = AppDatabase.Migration6To7::class),
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
    abstract fun tagDao(): TagDao

    @DeleteColumn(
        tableName = "video_mixer",
        columnName = "uri"
    )
    @DeleteColumn(
        tableName = "video_mixer",
        columnName = "trending_score"
    )
    class Migration2To3 : AutoMigrationSpec

    @DeleteColumn(
        tableName = "User",
        columnName = "drama"
    )
    @DeleteColumn(
        tableName = "User",
        columnName = "level"
    )
    class Migration5To6 : AutoMigrationSpec

    class Migration6To7 : AutoMigrationSpec {

        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            super.onPostMigrate(db)
            db.insertDefaultTags()
        }
    }
}
