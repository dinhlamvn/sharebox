package com.dinhlam.sharebox.extensions

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.screenWidth() = resources.displayMetrics.widthPixels

fun Context.screenWidth() = resources.displayMetrics.widthPixels

fun AppCompatActivity.screenHeight() = resources.displayMetrics.heightPixels

fun ComponentActivity.registerOnBackPressHandler(handler: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT,
            handler
        )
    } else {
        onBackPressedDispatcher.addCallback(this) {
            handler.invoke()
        }
    }
}

fun Activity.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(text, duration))
}

fun Activity.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text.takeIfNotNullOrBlank()?.let { toastContent ->
        Toast.makeText(this, toastContent, duration).show()
    }
}

fun Activity.hideKeyboard() = currentFocus?.let { focusedView ->
    getSystemServiceCompat<InputMethodManager>(Context.INPUT_METHOD_SERVICE).hideSoftInputFromWindow(
        focusedView.windowToken,
        0
    )
}
