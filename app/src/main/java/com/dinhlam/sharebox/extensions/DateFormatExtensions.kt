package com.dinhlam.sharebox.extensions

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.asProfileAge(): String {
    val subtractTime = nowUTCTimeInMillis() - this

    val timeOfYear = 365 * 24 * 3600 * 1000L
    val timeOfMonth = 30 * 24 * 3600 * 1000L
    val timeOfDay = 24 * 3600 * 1000L

    val years = subtractTime.div(timeOfYear)
    val months = subtractTime.mod(timeOfYear).div(timeOfMonth)
    val days = subtractTime.mod(timeOfYear).mod(timeOfMonth).div(timeOfDay)

    return when {
        years > 0 -> "$years y"
        months > 0 -> "$months mon"
        days > 0 -> "$days d"
        else -> "Recently"
    }
}

fun Long.asElapsedTimeDisplay(): String {
    val subtractTime = nowUTCTimeInMillis() - this

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
        months > 0 -> "${months}mon"
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> "a seconds ago"
    }
}

fun Long.format(format: String = "yyyy MMM d"): String {
    val df = SimpleDateFormat(format, Locale.US)
    return df.format(this)
}
