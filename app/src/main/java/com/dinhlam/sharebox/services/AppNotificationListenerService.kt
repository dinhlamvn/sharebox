package com.dinhlam.sharebox.services

import android.app.Notification
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@AndroidEntryPoint
class AppNotificationListenerService : NotificationListenerService() {

    private val coroutineScope by lazyOf(MainScope() + CoroutineName("AppNotificationListenerServiceScope") + Job())

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var userHelper: UserHelper

    companion object {
        private const val SERVICE_ID = 5102000
    }

    override fun onCreate() {
        super.onCreate()
        Logger.debug("Notification listener has been started.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                SERVICE_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(SERVICE_ID, createNotification())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val packageName = sbn.packageName
        if (!BuildConfig.DEBUG && packageName == this.packageName) {
            return
        }

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        val deeplink = extras.getString("deeplink")

        val packageInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(packageInfo).toString()

        val shareData = ShareData.ShareNotification(appName, title, text, deeplink)
        coroutineScope.launch(Dispatchers.IO) {
            shareRepository.insert(
                shareData = shareData,
                shareBoxId = userHelper.notificationsBoxId
            )
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
            .setContentText(getString(R.string.archive_notifications_notification_message))
            .setSubText(getString(R.string.title_archive_notification))
            .setSmallIcon(R.drawable.ic_notification).setAutoCancel(false).setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1122,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }
}