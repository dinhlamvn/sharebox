package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import com.dinhlam.sharebox.extensions.dp
import com.google.android.material.card.MaterialCardView

class CardViewIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

    private val imageView = AppCompatImageView(context)

    init {
        addView(imageView, LayoutParams(24.dp(), 24.dp()).apply {
            gravity = Gravity.CENTER
        })
    }

    fun setIcon(icon: Drawable) {
        imageView.setImageDrawable(icon)
    }
}