package com.dinhlam.sharebox.di

import android.content.Context
import androidx.room.Room
import com.dinhlam.sharebox.data.local.AppDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BookmarkCollectionDao
import com.dinhlam.sharebox.data.local.dao.BookmarkDao
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.ShareCommunityDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.dao.VideoMixerDao
import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class, ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class])
object DatabaseModule {

    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context, gson: Gson
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "share-box-db")
            .addTypeConverter(ShareDataConverter(gson)).build()
    }

    @Provides
    fun provideShareDao(
        appDatabase: AppDatabase
    ): ShareDao {
        return appDatabase.shareDao()
    }

    @Provides
    fun provideUserDao(
        appDatabase: AppDatabase
    ): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideLikeDao(
        appDatabase: AppDatabase
    ): LikeDao {
        return appDatabase.likeDao()
    }

    @Provides
    fun provideCommentDao(
        appDatabase: AppDatabase
    ): CommentDao {
        return appDatabase.commentDao()
    }

    @Provides
    fun provideBookmarkCollectionDao(
        appDatabase: AppDatabase
    ): BookmarkCollectionDao {
        return appDatabase.bookmarkCollectionDao()
    }

    @Provides
    fun provideBookmarkDao(
        appDatabase: AppDatabase
    ): BookmarkDao {
        return appDatabase.bookmarkDao()
    }

    @Provides
    fun provideShareCommunityDao(
        appDatabase: AppDatabase
    ): ShareCommunityDao {
        return appDatabase.shareCommunityDao()
    }

    @Provides
    fun provideVideoMixerDao(
        appDatabase: AppDatabase
    ): VideoMixerDao {
        return appDatabase.videoMixerDao()
    }
}
