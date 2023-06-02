package com.dinhlam.sharebox.extensions

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.view.isVisible

fun EditText.getTrimmedText() = text.trimmedString()

fun Editable?.string() = this?.toString() ?: ""

fun Editable?.trimmedString() = this?.toString()?.trim() ?: ""

fun TextView.setDrawableCompat(start: Int = 0, top: Int = 0, end: Int = 0, bottom: Int = 0) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

fun TextView.setDrawableCompat(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

fun TextView.setTextAndVisibleIfNonBlankText(text: String?) {
    text.takeIfNotNullOrBlank()?.let { nonBlankText ->
        isVisible = true
        this.text = nonBlankText
    } ?: apply {
        this.text = null
        isVisible = false
    }
}

@Suppress("DEPRECATION")
fun TextView.setTextAppearanceCompat(@StyleRes textAppearance: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(textAppearance)
    } else {
        setTextAppearance(context, textAppearance)
    }
}
