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
        Logger.debug("Realtime service is running.")
        realtimeDatabaseRepository.listen()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Logger.debug("Realtime service is running.")
        realtimeDatabaseRepository.listen()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.debug("Realtime service is stopped.")
        realtimeDatabaseRepository.release()
        return super.onUnbind(intent)
    }
}