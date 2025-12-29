package com.dinhlam.sharebox

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dinhlam.sharebox.callback.SimpleActivityLifecycleCallbacks
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.loader.GlideImageLoader
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.AppSettings
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.services.RealtimeServiceManager
import com.dinhlam.sharebox.tracking.TrackerManager
import com.dinhlam.sharebox.tracking.trackers.FirebaseAnalysisTracker
import com.dinhlam.sharebox.utils.UserUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@HiltAndroidApp
class ShareBoxApp : Application(), Configuration.Provider, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + CoroutineName("AppCoroutine") + SupervisorJob()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var userSharePref: UserSharePref

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var realtimeServiceManager: RealtimeServiceManager

    private fun createAnonymousUser() {
        if (!userHelper.isSignedIn() && userHelper.getCurrentUserId().isEmpty()) {
            FirebaseInstallations.getInstance().id.addOnSuccessListener { instanceId ->
                launch(Dispatchers.IO) {
                    val userId = UserUtils.createUserId(instanceId)
                    val user = userRepository.insert(
                        userId,
                        "Anonymous",
                        UserUtils.ANONYMOUS_AVATAR_URL,
                        true
                    )
                    user?.userId?.let(userSharePref::setAnonymousUserId)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        createAnonymousUser()
        Iconics.registerFont(GoogleMaterial)
        Iconics.registerFont(FontAwesome)
        requestApplyTheme()
        ImageLoader.setLoader(GlideImageLoader)

        Logger.debug("Current User ID: ${userHelper.getCurrentUserId()}")
        if (userHelper.isSignedIn()) {
            FirebaseCrashlytics.getInstance().setUserId(userHelper.getCurrentUserId())
        }

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

        TrackerManager.addTracker(FirebaseAnalysisTracker(this, userHelper.getCurrentUserId()))

        realtimeServiceManager.bindRealtimeService()

        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Logger.debug("My token: $token")
            }
            registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    super.onActivityCreated(activity, savedInstanceState)
                    Logger.debug("The activity ${activity.javaClass.simpleName} is created.")
                }
            })
        }
    }


    private fun requestApplyTheme() {
        when (appSettingHelper.getTheme()) {
            AppSettings.Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppSettings.Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
