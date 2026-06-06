package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SyncDataService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): SyncDataService = this@SyncDataService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        Logger.debug("Sync service is running.")
        WorkerUtils.enqueueJobSyncDataOneTime(applicationContext)
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Logger.debug("Sync service is running.")
        WorkerUtils.enqueueJobSyncDataOneTime(applicationContext)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.debug("Sync service is stopped.")
        return super.onUnbind(intent)
    }
}
