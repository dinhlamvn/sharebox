package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.Icons

class FontAwesomeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {


    init {
        context.obtainStyledAttributes(attrs, R.styleable.FontAwesomeImageView).use { typedArray ->
            val faIcon = typedArray.getString(R.styleable.FontAwesomeImageView_fa_icon)
            val gmIcon = typedArray.getString(R.styleable.FontAwesomeImageView_gm_icon)
            if (faIcon == null && gmIcon == null) {
                throw IllegalArgumentException("Must be set fa_icon or gm_icon")
            }
            faIcon?.let {
                setImageDrawable(Icons.getFontAwesomeIcon(context, it))
            } ?: setImageDrawable(Icons.getGoogleMaterialIcon(context, gmIcon!!))
        }
    }

    fun setFaIcon(iconCode: String) {
        setImageDrawable(Icons.getFontAwesomeIcon(context, iconCode))
    }

    fun setGmIcon(iconCode: String) {
        setImageDrawable(Icons.getGoogleMaterialIcon(context, iconCode))
    }
}