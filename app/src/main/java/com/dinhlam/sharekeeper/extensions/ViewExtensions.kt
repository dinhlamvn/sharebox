package com.dinhlam.sharekeeper.extensions

import android.widget.EditText

fun EditText.getTrimmedText() = text.toString().trim()