package com.dinhlam.sharebox.extensions

import android.content.Context
import android.util.TypedValue
import java.text.DecimalFormat

fun Number.dp(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).toInt()

fun Number.dpF(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
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

fun Int.takeIfNotZero() = takeIf { it > 0 }