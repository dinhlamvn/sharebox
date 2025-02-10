package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyncRealtimeDataService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): SyncRealtimeDataService = this@SyncRealtimeDataService
    }

    private val binder = LocalBinder()

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug("Realtime service is running.")
        realtimeDatabaseRepository.sync()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        realtimeDatabaseRepository.release()
        Logger.debug("Realtime service is stopped.")
    }
}