package com.dinhlam.sharesaver.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified R> Any?.cast(): R? {
    return this as? R
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableExtra(key, T::class.java)
    }
    return getParcelableExtra(key)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayListExtra(key, T::class.java)
    }
    return getParcelableArrayListExtra(key)
}