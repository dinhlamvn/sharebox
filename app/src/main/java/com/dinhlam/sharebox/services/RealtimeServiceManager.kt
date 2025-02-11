package com.dinhlam.sharebox.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userHelper: UserHelper
) {

    private lateinit var syncRealtimeDataService: SyncRealtimeDataService
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Logger.debug("Realtime service is connected.")
            val binder = service as SyncRealtimeDataService.LocalBinder
            syncRealtimeDataService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Logger.debug("Realtime service is disconnected.")
            isBound = false
        }
    }

    fun bindRealtimeService() {
        if (userHelper.isSignedIn()) {
            context.bindService(
                Intent(context, SyncRealtimeDataService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unbindRealtimeService() {
        if (isBound) {
            context.unbindService(connection)
        }
    }
}