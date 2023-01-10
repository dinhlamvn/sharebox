package com.dinhlam.sharebox.extensions

import android.widget.EditText

fun EditText.getTrimmedText() = text.toString().trim()
