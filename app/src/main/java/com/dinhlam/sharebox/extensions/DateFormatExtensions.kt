package com.dinhlam.sharebox.extensions

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Long.format(pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Long.formatForFeed(): String = format("hh:mm a • MMM dd, YYYY")

fun Long.asProfileAge(): String {
    val now = Calendar.getInstance()
    val subtractTime = now.timeInMillis - this

    val timeOfYear = 365 * 24 * 3600 * 1000L
    val timeOfMonth = 30 * 24 * 3600 * 1000L
    val timeOfDay = 24 * 3600 * 1000L

    val years = subtractTime / timeOfYear
    val months = (subtractTime % timeOfYear) / timeOfMonth
    val days = ((subtractTime % timeOfYear) % timeOfMonth) / timeOfDay

    return when {
        years > 0 -> "$years y"
        months > 0 -> "$months m"
        days > 0 -> "$days d"
        else -> "recently"
    }
}
