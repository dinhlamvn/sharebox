package com.dinhlam.sharebox.di

import android.content.Context
import androidx.room.Room
import com.dinhlam.sharebox.database.AppDatabase
import com.dinhlam.sharebox.database.dao.FolderDao
import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.dao.UserDao
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
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "share-box-db")
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
    fun provideFolderDao(
        appDatabase: AppDatabase
    ): FolderDao {
        return appDatabase.folderDao()
    }

    @Provides
    fun provideUserDao(
        appDatabase: AppDatabase
    ): UserDao {
        return appDatabase.userDao()
    }
}
