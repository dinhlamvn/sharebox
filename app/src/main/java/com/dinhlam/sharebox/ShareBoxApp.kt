package com.dinhlam.sharebox

import android.app.Application
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.loader.GlideImageLoader
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ShareBoxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ImageLoader.setLoader(GlideImageLoader)
    }
}
