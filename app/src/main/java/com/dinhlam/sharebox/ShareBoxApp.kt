package com.dinhlam.sharebox

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.loader.GlideImageLoader
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ShareBoxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ImageLoader.setLoader(GlideImageLoader)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelCompat.Builder(
                AppConsts.DEFAULT_NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
            ).setName("Sharebox Main Channel")
                .setDescription("This channel to notify some news to user").build()
            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
        }
    }
}
