package com.dinhlam.sharesaver.extensions

fun String?.takeIfNotNullOrBlank(): String? = takeIf { !it.isNullOrBlank() }