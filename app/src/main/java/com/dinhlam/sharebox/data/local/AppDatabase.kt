package com.dinhlam.sharebox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.converter.ShareModeConverter
import com.dinhlam.sharebox.data.local.converter.VideoSourceConverter
import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.ShareCommunityDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.dao.VideoMixerDao
import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.entity.Bookmark
import com.dinhlam.sharebox.data.local.entity.BookmarkCollection
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.local.entity.Folder
import com.dinhlam.sharebox.data.local.entity.HashTag
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.local.entity.ShareCommunity
import com.dinhlam.sharebox.data.local.entity.ShareHashTag
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.local.entity.VideoMixer
import com.dinhlam.sharebox.data.local.entity.Like

@Database(
    entities = [Share::class, Folder::class, HashTag::class, User::class, Like::class, ShareHashTag::class, Comment::class, BookmarkCollection::class, Bookmark::class, ShareCommunity::class, VideoMixer::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ShareModeConverter::class, ShareDataConverter::class, VideoSourceConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
    abstract fun userDao(): UserDao
    abstract fun likeDao(): LikeDao
    abstract fun commentDao(): CommentDao
    abstract fun bookmarkCollectionDao(): BookmarkCollectionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun shareCommunityDao(): ShareCommunityDao

    abstract fun videoMixerDao(): VideoMixerDao
}
