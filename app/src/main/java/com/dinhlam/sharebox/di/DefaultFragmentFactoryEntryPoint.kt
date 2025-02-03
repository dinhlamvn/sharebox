package com.dinhlam.sharebox.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent

@EntryPoint
@InstallIn(ActivityComponent::class, FragmentComponent::class)
interface DefaultFragmentFactoryEntryPoint {
    fun getFragmentFactory(): DefaultFragmentFactory
}