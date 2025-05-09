package com.dinhlam.sharebox.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
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

    private val systemAppPackages = arrayOf(
        "com.android.settings",                      // Cài đặt
        "com.android.systemui",                      // System UI
        "com.google.android.gms",                    // Google Play Services
        "com.android.vending",                       // Google Play Store
        "com.google.android.gsf",                    // Google Services Framework
        "com.android.dialer",                        // Ứng dụng điện thoại
        "com.android.contacts",                      // Danh bạ
        "com.google.android.apps.messaging",         // Tin nhắn (Messages)
        "com.google.android.inputmethod.latin",      // Bàn phím Gboard
        "com.android.launcher",                      // Trình khởi chạy (Launcher)
        "com.android.camera",                        // Camera mặc định
        "com.android.documentsui",                   // Trình quản lý file
        "com.android.mms",                           // Tin nhắn MMS cũ
        "com.android.phone",                         // Phone service
        "com.android.calendar",                      // Lịch
        "com.android.providers.settings",            // Cung cấp dữ liệu hệ thống
        "com.android.packageinstaller"               // Trình cài đặt gói
    )

    private val coroutineScope by lazyOf(MainScope() + CoroutineName("AppNotificationListenerServiceScope") + Job())

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var userHelper: UserHelper

    companion object {
        private const val SERVICE_ID = 5102000
        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("Notification listener has been started.")
        if (intent?.action == ACTION_START_SERVICE) {
            startForeground()
        } else {
            appSettingHelper.setRecordingNotifications(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {

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

        if (systemAppPackages.contains(packageName)) {
            // Don't store the system app messages
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
        val intent =
            Intent(this, AppNotificationListenerService::class.java).setAction(ACTION_STOP_SERVICE)

        return NotificationCompat.Builder(this, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
            .setContentText(getString(R.string.archive_notifications_notification_message))
            .setSubText(getString(R.string.title_archive_notification))
            .setSmallIcon(R.drawable.ic_notification).setAutoCancel(false).setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                0, getString(R.string.shutdown), PendingIntent.getService(
                    this, 1, intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }
}