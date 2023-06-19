package com.dinhlam.sharebox.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.services.CleanUpDataService
import java.util.Calendar

object AlarmUtils {

    fun scheduleToCleanUpData(context: Context) {
        val alarmManager = context.getSystemServiceCompat<AlarmManager>(Context.ALARM_SERVICE)
        val alarmIntent = Intent(context, CleanUpDataService::class.java).let { intent ->
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent
        )
    }
}