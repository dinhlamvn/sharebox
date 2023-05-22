package com.dinhlam.sharebox.extensions

import android.content.res.Resources
import android.util.TypedValue
import java.text.DecimalFormat

fun Number.dp() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
).toInt()

fun Number.dpF() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
)

fun Int.asDisplayCountValue(): String = if (this <= 99) {
    "$this"
} else {
    "99+"
}

fun Int.asViewMoreDisplayCountValue(): String = "+$this"

fun Int.asDisplayPoint(): String {
    return DecimalFormat("#,###").format(this)
}

fun Int.coerceMinMax(min: Int, max: Int) = this.coerceAtLeast(min).coerceAtMost(max)

fun Int?.orElse(other: Int) = this ?: other

fun Int.takeIfGreaterThanZero() = takeIf { it > 0 }