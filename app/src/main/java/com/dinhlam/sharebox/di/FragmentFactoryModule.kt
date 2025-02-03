package com.dinhlam.sharebox.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
class FragmentFactoryModule {
    @Provides
    fun provideFactoryMap(): Map<Class<out Fragment>, @JvmSuppressWildcards Provider<Fragment>> {
        return emptyMap()
    }
}