package com.dinhlam.sharebox.logger

import android.util.Log
import com.dinhlam.sharebox.BuildConfig

object Logger {

    private const val TAG = "ShareBox"

    private var enableLog = false

    private var logTag: String = TAG

    init {
        enableLog = BuildConfig.DEBUG
    }

    fun withTag(tag: String): Logger {
        return this.apply {
            logTag = tag
        }
    }

    fun debug(message: String) {
        if (enableLog) {
            Log.d(logTag, message)
        }
    }

    fun error(message: String) {
        if (enableLog) {
            Log.e(logTag, message)
        }
    }

    fun error(error: Throwable) {
        val message = error.message ?: return
        error(message)
    }

    fun warning(message: String, t: Throwable?) {
        if (enableLog) {
            Log.w(logTag, message, t)
        }
    }
}