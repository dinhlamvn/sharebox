package com.dinhlam.sharebox.extensions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.screenWidth() = resources.displayMetrics.widthPixels

fun Fragment.screenHeight() = resources.displayMetrics.heightPixels

fun Fragment.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(text, duration))
}

fun Fragment.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text.takeIfNotNullOrBlank()?.let { toastContent ->
        Toast.makeText(requireContext(), toastContent, duration).show()
    }
}
