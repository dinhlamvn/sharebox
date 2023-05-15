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

    val years = subtractTime.div(timeOfYear)
    val months = subtractTime.mod(timeOfYear).div(timeOfMonth)
    val days = subtractTime.mod(timeOfYear).mod(timeOfMonth).div(timeOfDay)

    return when {
        years > 0 -> "$years y"
        months > 0 -> "$months m"
        days > 0 -> "$days d"
        else -> "Recently"
    }
}

fun Long.asCommentDisplayTime(): String {
    val now = Calendar.getInstance()
    val subtractTime = now.timeInMillis - this

    val timeOfYear = 365 * 24 * 3600 * 1000L
    val timeOfMonth = 30 * 24 * 3600 * 1000L
    val timeOfDay = 24 * 3600 * 1000L
    val timeOfHour = 3600 * 1000L
    val timeOfMinute = 60 * 1000L

    val years = subtractTime.div(timeOfYear)
    val months = subtractTime.mod(timeOfYear).div(timeOfMonth)
    val days = subtractTime.mod(timeOfYear).mod(timeOfMonth).div(timeOfDay)
    val hours = subtractTime.mod(timeOfYear).mod(timeOfMonth).mod(timeOfDay).div(timeOfHour)
    val minutes = subtractTime.mod(timeOfYear).mod(timeOfMonth).mod(timeOfDay).mod(timeOfHour)
        .div(timeOfMinute)

    return when {
        years > 0 -> "${years}y"
        months > 0 -> "${months}m"
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> "a seconds ago"
    }
}
