package com.dinhlam.sharebox.utils

import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardUtil {

    fun hideKeyboard(view: View) {
        val imm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.context.getSystemService(InputMethodManager::class.java)
        } else {
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}