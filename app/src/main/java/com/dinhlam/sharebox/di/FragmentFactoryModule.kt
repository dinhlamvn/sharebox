package com.dinhlam.sharebox.di

import androidx.fragment.app.Fragment
import com.dinhlam.sharebox.ui.home.HomeFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class FragmentFactoryModule {
    @Binds
    @IntoMap
    @FragmentKey(HomeFragment::class)
    abstract fun bindHomeFragment(fragment: HomeFragment): Fragment
}