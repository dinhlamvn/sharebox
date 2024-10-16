package com.dinhlam.sharebox.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PerformCopiedContentService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 20241002
    }

    @Inject
    lateinit var router: Router

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val clipboardManager =
            getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)

        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = router.shareLink(this).putExtra(AppExtras.EXTRA_BOOLEAN, true)
        return NotificationCompat.Builder(this, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NOTIFICATION_ID,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setContentText(getString(R.string.perform_copied_content_desc))
            .addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.archives),
                    PendingIntent.getActivity(
                        this,
                        NOTIFICATION_ID,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
            .build()
    }
}