package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getColorCompat

class FontAwesomeIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    enum class IconStyle {
        REGULAR, BRANDS_REGULAR, SOLID
    }

    private var iconSize: Int = 20.dp
    private var iconCode: String = ""
    private var iconStyle: IconStyle = IconStyle.SOLID

    @ColorInt
    private var iconColor: Int = context.getColorCompat(R.color.md_theme_primary)

    init {
        gravity = Gravity.CENTER
        context.obtainStyledAttributes(attrs, R.styleable.FontAwesomeIconView).use { typedArray ->
            iconCode = typedArray.getString(R.styleable.FontAwesomeIconView_icon_code).orEmpty()
            setIconCode(iconCode)

            iconSize =
                typedArray.getDimensionPixelSize(R.styleable.FontAwesomeIconView_icon_size, 20.dp)
            setIconSize(iconSize)

            iconStyle = when (typedArray.getInt(R.styleable.FontAwesomeIconView_icon_style, 0)) {
                1 -> IconStyle.REGULAR
                2 -> IconStyle.BRANDS_REGULAR
                else -> IconStyle.SOLID
            }
            invalidateTextStyle()

            iconColor = typedArray.getColor(
                R.styleable.FontAwesomeIconView_icon_color,
                context.getColorCompat(R.color.md_theme_primary)
            )
            invalidateIconColor()
        }
    }

    private fun invalidateIcon() {
        if (iconCode.isBlank()) {
            super.setText(null)
            return
        }
        val hexVal = Integer.parseInt(iconCode, 16)
        val str = buildString {
            append(hexVal.toChar())
        }
        super.setText(str)
    }

    private fun invalidateTextStyle() {
        val textStyle = when (iconStyle) {
            IconStyle.REGULAR -> R.style.TextFontAwesomeRegular
            IconStyle.BRANDS_REGULAR -> R.style.TextFontAwesomeBrandsRegular
            else -> R.style.TextFontAwesomeSolid
        }
        super.setTextAppearance(context, textStyle)
        setIconSize(iconSize)
        invalidateIconColor()
    }

    private fun invalidateIconColor() {
        super.setTextColor(iconColor)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("throw IllegalStateException(\"No support\")"))
    override fun setTextAppearance(context: Context?, resId: Int) {
        throw IllegalStateException("No support")
    }

    override fun setTextAppearance(resId: Int) {
        throw IllegalStateException("No support")
    }

    fun setIconColor(@ColorInt color: Int) {
        iconColor = color
        invalidateIconColor()
    }

    fun setIconCode(iconCode: String) {
        this.iconCode = iconCode
        invalidateIcon()
    }

    fun setIconSize(iconSize: Int) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize * 1F)
    }

    fun setIconStyle(iconStyle: IconStyle) {
        this.iconStyle = iconStyle
        invalidateTextStyle()
    }
}