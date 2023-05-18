package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewLoadingBinding

class ShareBoxLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: ViewLoadingBinding = inflate(context, R.layout.view_loading, this).run {
        setBackgroundColor(Color.argb(0, 0, 0, 0))
        isFocusable = true
        isClickable = true
        ViewLoadingBinding.bind(this).apply {
            progressBar.hide()
            isVisible = false
        }
    }

    fun show() {
        binding.progressBar.show()
        isVisible = true
    }

    fun hide() {
        binding.progressBar.hide()
        isVisible = false
    }
}