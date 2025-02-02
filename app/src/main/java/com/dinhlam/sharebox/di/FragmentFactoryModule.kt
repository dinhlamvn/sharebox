package com.dinhlam.sharebox.di

import androidx.fragment.app.Fragment
import com.dinhlam.sharebox.ui.discover.DiscoverFragment
import com.dinhlam.sharebox.ui.download.DownloadFragment
import com.dinhlam.sharebox.ui.home.HomeFragment
import com.dinhlam.sharebox.ui.profile.ProfileFragment
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

    @Binds
    @IntoMap
    @FragmentKey(DiscoverFragment::class)
    abstract fun bindDiscoverFragment(fragment: DiscoverFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DownloadFragment::class)
    abstract fun bindDownloadFragment(fragment: DownloadFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ProfileFragment::class)
    abstract fun bindProfileFragment(fragment: ProfileFragment): Fragment
}