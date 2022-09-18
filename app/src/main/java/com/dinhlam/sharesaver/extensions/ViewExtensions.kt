package com.dinhlam.sharesaver.extensions

import android.widget.EditText

fun EditText.getTrimmedText() = text.toString().trim()