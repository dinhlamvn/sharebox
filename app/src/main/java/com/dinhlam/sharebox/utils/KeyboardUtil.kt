package com.dinhlam.sharebox.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.dinhlam.sharebox.extensions.getSystemServiceCompat

object KeyboardUtil {

    fun hideKeyboard(view: View) {
        val imm =
            view.context.getSystemServiceCompat<InputMethodManager>(Context.INPUT_METHOD_SERVICE)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun copyTextToClipboard(context: Context, text: String) {
        val clipboardManager =
            context.getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("share_box_recovery_password", text))
    }

    fun getTextFromClipboard(context: Context): String {
        val clipboardManager =
            context.getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
        val primaryClip = clipboardManager.primaryClip ?: return ""
        return if (primaryClip.itemCount > 0) {
            primaryClip.getItemAt(0).text.toString()
        } else {
            ""
        }
    }
}