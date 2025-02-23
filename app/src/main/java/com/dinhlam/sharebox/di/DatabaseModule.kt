package com.dinhlam.sharebox.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dinhlam.sharebox.data.local.AppDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.TagDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.extensions.insertDefaultTags
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(value = [SingletonComponent::class])
object DatabaseModule {


    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context, gson: Gson
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "share-box-db")
            .fallbackToDestructiveMigration()
            .addTypeConverter(ShareDataConverter(gson))
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.insertDefaultTags()
                    }
                }
            })
            .build()
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
    fun provideBoxDao(
        appDatabase: AppDatabase
    ): BoxDao {
        return appDatabase.boxDao()
    }

    @Provides
    fun provideTagDa(
        appDatabase: AppDatabase
    ): TagDao {
        return appDatabase.tagDao()
    }
}
