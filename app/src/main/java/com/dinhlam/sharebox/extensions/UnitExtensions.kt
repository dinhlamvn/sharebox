package com.dinhlam.sharebox.extensions

import android.content.Context
import android.util.TypedValue

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
