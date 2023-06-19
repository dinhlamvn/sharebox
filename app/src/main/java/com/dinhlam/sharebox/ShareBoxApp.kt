package com.dinhlam.sharebox

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.loader.GlideImageLoader
import com.dinhlam.sharebox.services.CleanUpDataService
import com.dinhlam.sharebox.utils.AlarmUtils
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ShareBoxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ContextCompat.startForegroundService(
            this,
            Intent(this, CleanUpDataService::class.java)
        )
        ImageLoader.setLoader(GlideImageLoader)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelCompat.Builder(
                AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
            ).setName("Main Channel").setDescription("This channel to notify news to user").build()

            val notificationDownloadChannel = NotificationChannelCompat.Builder(
                AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW
            ).setName("Download channel")
                .setDescription("This channel to notify while download file from network").build()

            NotificationManagerCompat.from(this).createNotificationChannelsCompat(
                listOf(
                    notificationChannel, notificationDownloadChannel
                )
            )
        }
    }
}
