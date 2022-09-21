package com.dinhlam.sharesaver.di

import android.content.Context
import androidx.room.Room
import com.dinhlam.sharesaver.database.AppDatabase
import com.dinhlam.sharesaver.database.dao.FolderDao
import com.dinhlam.sharesaver.database.dao.ShareDao
import com.dinhlam.sharesaver.repository.FolderRepository
import com.dinhlam.sharesaver.repository.ShareRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(value = [ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class])
object DatabaseModule {

    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "share-keeper-db").build()
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
}