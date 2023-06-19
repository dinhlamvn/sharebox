package com.dinhlam.sharebox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.services.CleanUpDataService

class CleanUpDataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            context?.let { ctx ->
                ContextCompat.startForegroundService(
                    ctx,
                    Intent(ctx, CleanUpDataService::class.java)
                )
            }
        }
    }
}