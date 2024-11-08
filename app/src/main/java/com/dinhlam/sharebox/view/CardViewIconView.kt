package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.utils.Icons
import com.google.android.material.card.MaterialCardView
import com.mikepenz.iconics.utils.colorInt

class CardViewIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

    companion object {
        private const val DEFAULT_ICON_SIZE = 24
    }

    private var iconSize: Int = 24.dp()
    private var iconCode: String? = null
    private var iconColor: Int = 0

    private val imageView = AppCompatImageView(context)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CardViewIconView).use { typedArray ->
            iconSize = typedArray.getDimensionPixelSize(
                R.styleable.CardViewIconView_icon_size,
                DEFAULT_ICON_SIZE.dp()
            )

            iconCode = typedArray.getString(R.styleable.CardViewIconView_icon_code)

            iconColor = typedArray.getColor(R.styleable.CardViewIconView_icon_color, 0)
        }

        addView(imageView, LayoutParams(iconSize, iconSize).apply {
            gravity = Gravity.CENTER
        })

        iconCode?.let { code ->
            when {
                code.startsWith("fa") -> imageView.setImageDrawable(
                    Icons.getFontAwesomeIcon(
                        context,
                        code
                    ).apply {
                        if (iconColor != 0) {
                            colorInt = iconColor
                        }
                    }
                )

                code.startsWith("gm") -> imageView.setImageDrawable(
                    Icons.getGoogleMaterialIcon(
                        context,
                        code
                    ).apply {
                        if (iconColor != 0) {
                            colorInt = iconColor
                        }
                    }
                )
            }
        }
    }

    fun setIcon(icon: Drawable) {
        imageView.setImageDrawable(icon)
    }
}