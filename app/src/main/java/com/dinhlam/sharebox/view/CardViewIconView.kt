package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.dp
import com.google.android.material.card.MaterialCardView

class CardViewIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

    companion object {
        private const val DEFAULT_ICON_SIZE = 24
    }

    private var iconSize: Int = 24.dp()

    private val imageView = AppCompatImageView(context)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CardViewIconView).use { typedArray ->
            iconSize = typedArray.getDimensionPixelSize(
                R.styleable.CardViewIconView_icon_size,
                DEFAULT_ICON_SIZE.dp()
            )
        }

        addView(imageView, LayoutParams(iconSize, iconSize).apply {
            gravity = Gravity.CENTER
        })
    }

    fun setIcon(icon: Drawable) {
        imageView.setImageDrawable(icon)
    }
}