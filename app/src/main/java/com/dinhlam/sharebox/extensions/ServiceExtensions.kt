package com.dinhlam.sharebox.extensions

import android.app.ActivityManager
import android.content.Context

@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemServiceCompat<ActivityManager>(Context.ACTIVITY_SERVICE)
    return activityManager.getRunningServices(Int.MAX_VALUE)
        .any { runningServiceInfo -> runningServiceInfo.service.className == serviceClassName }
}