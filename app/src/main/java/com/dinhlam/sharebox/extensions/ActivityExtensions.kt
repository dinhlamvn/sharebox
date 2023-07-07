package com.dinhlam.sharebox.extensions

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Context.screenWidth() = resources.displayMetrics.widthPixels

fun Context.screenHeight() = resources.displayMetrics.heightPixels

fun Context.widthPercentage(@IntRange(from = 1, to = 100) percent: Int): Int {
    return screenWidth().times(percent.div(100))
}

fun Context.heightPercentage(@IntRange(from = 1, to = 100) percent: Int): Int {
    return screenHeight().times(percent.div(100))
}
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

fun Activity.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(text, duration))
}

fun Activity.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT): Toast? {
    return text.takeIfNotNullOrBlank()?.let { toastContent ->
        val toast = Toast.makeText(this, toastContent, duration)
        toast.show()
        toast
    }
}

fun Activity.hideKeyboard() = currentFocus?.let { focusedView ->
    getSystemServiceCompat<InputMethodManager>(Context.INPUT_METHOD_SERVICE).hideSoftInputFromWindow(
        focusedView.windowToken, 0
    )
}

fun EditText.hideKeyboard() {
    context.getSystemServiceCompat<InputMethodManager>(Context.INPUT_METHOD_SERVICE)
        .hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.showKeyboard() {
    context.getSystemServiceCompat<InputMethodManager>(Context.INPUT_METHOD_SERVICE)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
