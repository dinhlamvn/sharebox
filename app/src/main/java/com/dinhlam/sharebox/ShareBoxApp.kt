package com.dinhlam.sharebox

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.extensions.isServiceRunning
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.loader.GlideImageLoader
import com.dinhlam.sharebox.services.RealtimeDatabaseService
import com.dinhlam.sharebox.utils.WorkerUtils
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class ShareBoxApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    override fun onCreate() {
        super.onCreate()
        Iconics.registerFont(GoogleMaterial)
        Iconics.registerFont(FontAwesome)
        requestApplyTheme()
        ImageLoader.setLoader(GlideImageLoader)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelCompat.Builder(
                AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
            ).setName("Main Channel").setDescription("This channel to notify news to user").build()

            val notificationDownloadChannel = NotificationChannelCompat.Builder(
                AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW
            ).setName("Download channel")
                .setDescription("This channel to notify while download file from network").build()

            val notificationSyncDataChannel = NotificationChannelCompat.Builder(
                AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW
            ).setName("Sync data channel").setDescription("This channel to notify while sync data")
                .build()

            NotificationManagerCompat.from(this).createNotificationChannelsCompat(
                listOf(
                    notificationChannel, notificationDownloadChannel, notificationSyncDataChannel
                )
            )
        }

        WorkerUtils.enqueueSyncUserData(this)
        WorkerUtils.enqueueCleanUpOldData(this)
        startRealtimeDatabaseService()
    }

    private fun requestApplyTheme() {
        when (appSettingHelper.getTheme()) {
            AppSettings.Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppSettings.Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(workerFactory).build()
    }

    private fun startRealtimeDatabaseService() {
        if (isServiceRunning(RealtimeDatabaseService::class.java.name)) {
            return
        }
        ContextCompat.startForegroundService(
            this, Intent(this, RealtimeDatabaseService::class.java)
        )
    }
}
