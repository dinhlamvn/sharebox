package com.dinhlam.sharesaver.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dinhlam.sharesaver.extensions.cast

abstract class SharePref constructor(
    context: Context,
    sharePrefName: String
) {

    protected val sharePref: SharedPreferences =
        context.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)

    fun put(key: String, value: Any, sync: Boolean = false) {
        if (key.isBlank()) {
            error("Key is required")
        }
        sharePref.edit(sync) {
            when (value) {
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Set<*> -> putStringSet(key, value.cast())
                else -> error("Not support put for value: $value")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String, typeOf: Class<T>, default: T): T {
        if (key.isBlank()) {
            error("Key is required")
        }
        return when {
            typeOf.isAssignableFrom(Int::class.java) -> sharePref.getInt(key, default.cast()!!) as T
            typeOf.isAssignableFrom(Float::class.java) -> sharePref.getFloat(
                key,
                default.cast()!!
            ) as T
            typeOf.isAssignableFrom(Long::class.java) -> sharePref.getLong(
                key,
                default.cast()!!
            ) as T
            typeOf.isAssignableFrom(String::class.java) -> sharePref.getString(
                key,
                default.cast()!!
            ) as T
            typeOf.isAssignableFrom(Set::class.java) -> sharePref.getStringSet(
                key,
                default.cast()!!
            ) as T
            else -> error("Not value for key: $key")
        }
    }
}
