package com.dinhlam.sharebox.extensions

import android.app.Activity
import android.content.DialogInterface.OnClickListener
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

fun Activity.showAlert(
    title: CharSequence? = null,
    message: CharSequence? = null,
    posBtnText: CharSequence? = null,
    negBtnText: CharSequence? = null,
    onPosClickListener: OnClickListener? = null,
    onNegClickListener: OnClickListener? = null
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)

    posBtnText?.let {
        builder.setPositiveButton(it, onPosClickListener)
    }

    negBtnText?.let {
        builder.setNegativeButton(it, onNegClickListener)
    }

    builder.create().show()
}

fun Activity.showAlert(
    @StringRes title: Int = 0,
    @StringRes message: Int = 0,
    @StringRes posBtnText: Int = 0,
    @StringRes negBtnText: Int = 0,
    onPosClickListener: OnClickListener? = null,
    onNegClickListener: OnClickListener? = null
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)

    if (posBtnText != 0) {
        builder.setPositiveButton(posBtnText, onPosClickListener)
    }

    if (negBtnText != 0) {
        builder.setPositiveButton(negBtnText, onNegClickListener)
    }

    builder.create().show()
}
