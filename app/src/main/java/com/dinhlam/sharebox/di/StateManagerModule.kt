package com.dinhlam.sharebox.di

import com.dinhlam.sharebox.base.StateManager
import com.dinhlam.sharebox.base.StateManagerReal
import com.dinhlam.sharebox.di.qualifier.BoxListStateManager
import com.dinhlam.sharebox.ui.boxlist.BoxListState
import com.dinhlam.sharebox.ui.boxlist.BoxListUpdateStateHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(
    value = [SingletonComponent::class]
)
object StateManagerModule {

    @Provides
    @BoxListStateManager
    fun provideBoxListStateManager(): StateManager<BoxListState> =
        StateManagerReal(BoxListState(), BoxListUpdateStateHandler())
}