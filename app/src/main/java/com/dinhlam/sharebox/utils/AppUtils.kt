package com.dinhlam.sharebox.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object AppUtils {

    fun hasNotificationAccess(context: Context): Boolean {
        val packageName = context.packageName
        val enabledListeners =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(packageName)
    }
}