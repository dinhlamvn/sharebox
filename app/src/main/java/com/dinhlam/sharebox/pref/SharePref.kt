package com.dinhlam.sharebox.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dinhlam.sharebox.extensions.castNonNull

abstract class SharePref constructor(
    context: Context,
    sharePrefName: String
) {

    private val sharePref: SharedPreferences =
        context.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)

    protected fun put(key: String, value: Any, sync: Boolean = false) {
        if (key.isBlank()) {
            error("Key is required")
        }
        sharePref.edit(sync) {
            when (value) {
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Set<*> -> {
                    val hasNonStringValue = value.any { it !is String }
                    if (hasNonStringValue) {
                        error("Not support put for value: $value")
                    }
                    putStringSet(key, value.castNonNull())
                }

                else -> error("Not support put for value: $value")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> get(key: String, default: T): T {
        if (key.isBlank()) {
            error("Key is required")
        }
        return sharePref.all[key] as? T ?: default
    }
}
