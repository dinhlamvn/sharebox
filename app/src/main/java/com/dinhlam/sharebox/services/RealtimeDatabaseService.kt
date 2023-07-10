package com.dinhlam.sharebox.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RealtimeDatabaseService : Service() {

    companion object {
        private const val SERVICE_ID = 69919090
    }

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    override fun onCreate() {
        super.onCreate()
        Logger.debug("$this is created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("$this is start command")
        startForeground(
            SERVICE_ID,
            NotificationCompat.Builder(this, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(getString(R.string.realtime_database_service_noti_content))
                .setSubText(getString(R.string.realtime_database_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        packageManager.getLaunchIntentForPackage(packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        )
        realtimeDatabaseRepository.consume()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Logger.debug("$this is on task removed")
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.debug("$this has been stopped")
        realtimeDatabaseRepository.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}