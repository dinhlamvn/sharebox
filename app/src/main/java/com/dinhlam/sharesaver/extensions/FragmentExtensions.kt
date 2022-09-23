package com.dinhlam.sharesaver.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.screenWidth() = resources.displayMetrics.widthPixels

fun Fragment.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text.takeIfNotNullOrBlank()?.let { toastContent ->
        Toast.makeText(requireContext(), toastContent, duration).show()
    }
}