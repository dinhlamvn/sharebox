package com.dinhlam.sharesaver.extensions

fun String?.takeIfNotNullOrBlank(): String? = takeIf { !it.isNullOrBlank() }

fun String.isWebLink(): Boolean {
    val regex =
        Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
    return regex.matches(this)
}