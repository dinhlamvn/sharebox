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
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.ui.clipboard.ClipboardDownloadShareActivity

class PerformCopiedContentService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 20241002
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val clipboardManager =
            getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
        clipboardManager.addPrimaryClipChangedListener {
            val a = 10
        }

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
        val intent = Intent(this, ClipboardDownloadShareActivity::class.java)
        return NotificationCompat.Builder(this, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.perform_copied_content_desc))
            .addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.download),
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