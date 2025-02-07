package com.dinhlam.sharebox.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyncRealtimeDataService : Service() {

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        realtimeDatabaseRepository.sync()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        realtimeDatabaseRepository.release()
    }
}