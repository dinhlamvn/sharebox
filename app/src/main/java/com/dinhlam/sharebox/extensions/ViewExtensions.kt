package com.dinhlam.sharebox.extensions

import android.widget.EditText
import android.widget.TextView

fun EditText.getTrimmedText() = text.toString().trim()

fun TextView.setDrawableCompat(start: Int = 0, top: Int = 0, end: Int = 0, bottom: Int = 0) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}
