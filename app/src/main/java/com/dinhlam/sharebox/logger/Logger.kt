package com.dinhlam.sharebox.logger

import android.util.Log
import com.dinhlam.sharebox.BuildConfig

object Logger {

    private const val TAG = "ShareBox"

    private var enableLog = false

    init {
        enableLog = BuildConfig.DEBUG
    }

    fun debug(message: String) {
        if (enableLog) {
            Log.d(TAG, message)
        }
    }

    fun warning(message: String, t: Throwable?) {
        if (enableLog) {
            Log.w(TAG, message, t)
        }
    }
}