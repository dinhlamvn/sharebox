package com.dinhlam.sharesaver.extensions

import android.app.Activity
import android.os.Build
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.addCallback

fun ComponentActivity.registerOnBackPressHandler(handler: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT, handler
        )
    } else {
        onBackPressedDispatcher.addCallback(this) {
            handler.invoke()
        }
    }
}

fun Activity.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text.takeIfNotNullOrBlank()?.let { toastContent ->
        Toast.makeText(this, toastContent, duration).show()
    }
}