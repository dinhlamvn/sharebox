package com.dinhlam.sharebox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dinhlam.sharebox.utils.WorkerUtils

class CleanUpDataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            context?.let { ctx ->
                WorkerUtils.enqueueJobSyncDataOneTime(ctx)
            }
        }
    }
}