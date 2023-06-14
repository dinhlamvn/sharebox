package com.dinhlam.sharebox.extensions

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun EditText.getTrimmedText() = text.trimmedString()

fun Editable?.string() = this?.toString() ?: ""

fun Editable?.trimmedString() = this?.toString()?.trim() ?: ""

fun TextView.setDrawableCompat(start: Int = 0, top: Int = 0, end: Int = 0, bottom: Int = 0) {
    setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

fun TextView.setDrawableCompat(
    start: Drawable? = null, top: Drawable? = null, end: Drawable? = null, bottom: Drawable? = null
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

fun TextView.doAfterTextChangedDebounce(
    waitMs: Long = 300, scope: CoroutineScope, action: (Editable?) -> Unit
) {
    addTextChangedListener(object : TextWatcher {
        var debounceJob: Job? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(waitMs)
                action(s)
            }
        }
    })
}
