package com.dinhlam.sharebox.di

import android.content.Context
import androidx.room.Room
import com.dinhlam.sharebox.data.local.AppDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.dao.VoteDao
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
        @ApplicationContext context: Context,
        gson: Gson
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "share-box-db")
            .addTypeConverter(ShareDataConverter(gson))
            .fallbackToDestructiveMigrationFrom()
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
    fun provideVoteDao(
        appDatabase: AppDatabase
    ): VoteDao {
        return appDatabase.voteDao()
    }
}
