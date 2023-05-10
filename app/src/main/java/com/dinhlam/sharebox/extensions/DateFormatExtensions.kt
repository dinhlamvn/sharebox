package com.dinhlam.sharebox.extensions

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.format(pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Long.formatForFeed(): String = format("hh:mm a • MMM dd, YYYY")
