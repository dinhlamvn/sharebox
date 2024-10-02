package com.dinhlam.sharebox.extensions

import android.app.Activity
import android.app.Notification
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

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


fun Context.pushNotification(id: Int, notification: Notification) {
    if (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(this).notify(id, notification)
    }
}

fun Context.cancelNotification(id: Int) {
    NotificationManagerCompat.from(this).cancel(id)
}