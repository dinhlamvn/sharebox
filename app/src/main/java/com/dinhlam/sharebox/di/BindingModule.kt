package com.dinhlam.sharebox.di

import com.dinhlam.sharebox.downloader.Downloader
import com.dinhlam.sharebox.downloader.FacebookDownloader
import com.dinhlam.sharebox.downloader.TiktokDownloader
import com.dinhlam.sharebox.downloader.TiktokDownloaderV2
import com.dinhlam.sharebox.downloader.YoutubeDownloader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
interface BindingModule {

    @Binds
    @Named("TiktokDownloader")
    fun bindTiktokDownloader(downloader: TiktokDownloader): Downloader

    @Binds
    @Named("TiktokDownloaderV2")
    fun bindTiktokDownloaderV2(downloader: TiktokDownloaderV2): Downloader

    @Binds
    @Named("FacebookDownloader")
    fun bindFacebookDownloader(downloader: FacebookDownloader): Downloader

    @Binds
    @Named("YoutubeDownloader")
    fun bindYoutubeDownloader(downloader: YoutubeDownloader): Downloader
}