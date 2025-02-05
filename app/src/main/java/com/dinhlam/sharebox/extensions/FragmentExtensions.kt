package com.dinhlam.sharebox.extensions

import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle

val Fragment.screenWidth
    get() = resources.displayMetrics.widthPixels

fun Fragment.widthPercentage(@IntRange(from = 1, to = 100) percent: Int): Int {
    return screenWidth.times(percent.div(100f)).toInt()
}

fun Fragment.heightPercentage(@IntRange(from = 1, to = 100) percent: Int): Int {
    return screenHeight.times(percent.div(100f)).toInt()
}

val Fragment.screenHeight
    get() = resources.displayMetrics.heightPixels

fun Fragment.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(text, duration))
}

fun Fragment.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text.takeIfNotNullOrBlank()?.let { toastContent ->
        Toast.makeText(requireContext(), toastContent, duration).show()
    }
}

fun <T> SavedStateHandle.getNonNull(key: String): T =
    get<T>(key) ?: error("The value of $key is null.")

fun <T> SavedStateHandle.getNonNullOrElse(key: String, default: T): T =
    get<T>(key) ?: default

val Fragment.packageName: String
    get() = context!!.packageName