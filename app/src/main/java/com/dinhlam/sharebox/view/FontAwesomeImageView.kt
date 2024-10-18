package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.Icons
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorInt

class FontAwesomeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private var faIcon: String? = null
    private var gmIcon: String? = null


    init {
        context.obtainStyledAttributes(attrs, R.styleable.FontAwesomeImageView).use { typedArray ->
            faIcon = typedArray.getString(R.styleable.FontAwesomeImageView_fa_icon)
            gmIcon = typedArray.getString(R.styleable.FontAwesomeImageView_gm_icon)
            if (faIcon == null && gmIcon == null) {
                throw IllegalArgumentException("Must be set fa_icon or gm_icon")
            }
            setImageDrawable(getIcon())
        }
    }

    private fun getIcon(): IconicsDrawable {
        return faIcon?.let {
            Icons.getFontAwesomeIcon(context, it)
        } ?: Icons.getGoogleMaterialIcon(context, gmIcon!!)
    }

    fun setFaIcon(iconCode: String) {
        this.faIcon = iconCode
        this.gmIcon = null
        setImageDrawable(getIcon())
    }

    fun setGmIcon(iconCode: String) {
        this.gmIcon = iconCode
        this.faIcon = null
        setImageDrawable(getIcon())
    }

    fun setIconColor(@ColorInt color: Int) {
        setImageDrawable(getIcon().apply {
            colorInt = color
        })
    }
}