package com.dinhlam.sharebox.extensions

import java.math.BigInteger
import java.security.MessageDigest

fun String?.takeIfNotNullOrBlank(): String? = takeIf { !it.isNullOrBlank() }

fun String.isWebLink(): Boolean {
    val regex =
        Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
    return regex.matches(this)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("md5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun String.takeWithEllipsizeEnd(n: Int): String {
    return if (length < n) {
        take(n)
    } else {
        "${take(n)}..."
    }
}

fun String.appendIf(append: String, condition: (String) -> Boolean): String {
    return if (condition.invoke(this)) {
        "$this$append"
    } else {
        this
    }
}
