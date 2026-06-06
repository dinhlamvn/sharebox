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
class SyncDataServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userHelper: UserHelper
) {

    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Logger.debug("Sync service is connected.")
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Logger.debug("Sync service is disconnected.")
            isBound = false
        }
    }

    fun bindSyncService() {
        if (isBound || !userHelper.isSignedIn()) {
            return
        }
        context.bindService(
            Intent(context, SyncDataService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindSyncService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}
