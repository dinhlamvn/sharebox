package com.dinhlam.sharebox.extensions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.VibratorManager
import java.io.Serializable

inline fun <reified R> Any?.cast(): R? {
    return this as? R
}

inline fun <reified R> Any?.castNonNull(): R {
    return this as R
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableExtra(key, T::class.java)
    }
    return getParcelableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getSerializableExtra(key, T::class.java)
    }
    return getSerializableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayListExtra(key, T::class.java)
    }
    return getParcelableArrayListExtra(key)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayList(key, T::class.java)
    }
    return getParcelableArrayList(key)
}

inline fun <reified T> Context.getSystemServiceCompat(name: String): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getSystemService(T::class.java)
    } else {
        getSystemService(name) as T
    }
}

inline fun <reified T : Enum<T>> enumByNameIgnoreCase(input: String, default: T): T {
    return enumValues<T>().firstOrNull { enum -> enum.name.equals(input, true) } ?: default
}

@Suppress("DEPRECATION")
fun Context.vibrate(timing: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemServiceCompat<VibratorManager>(Context.VIBRATOR_MANAGER_SERVICE).defaultVibrator
    } else {
        getSystemServiceCompat(Context.VIBRATOR_SERVICE)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(timing, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(timing)
    }
}
