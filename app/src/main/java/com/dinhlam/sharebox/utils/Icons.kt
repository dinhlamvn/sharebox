package com.dinhlam.sharebox.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import com.dinhlam.sharebox.R
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp

object Icons {

    data class IconBuilder(
        val context: Context, val sizeDp: Int = 24, @ColorRes val colorRes: Int = 0
    ) {
        val color
            get() = if (colorRes != 0) colorRes else getIconColor(context)
    }

    private fun createIcon(
        context: Context, icon: IIcon, builder: IconBuilder = IconBuilder(context)
    ): IconicsDrawable {
        return IconicsDrawable(
            context, icon
        ).apply {
            sizeDp = builder.sizeDp
            colorRes = builder.color
        }
    }

    fun settingIcon(
        context: Context
    ): IconicsDrawable {
        return createIcon(
            context,
            GoogleMaterial.Icon.gmd_settings,
        )
    }

    fun addIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_add_circle, block(IconBuilder(context))
        )
    }

    fun clearIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_clear, block(IconBuilder(context))
        )
    }

    fun visibilityOnIcon(context: Context): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_visibility
        )
    }

    fun visibilityOffIcon(context: Context): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_visibility_off
        )
    }

    fun editIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_edit, block(IconBuilder(context))
        )
    }

    fun openIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_open_in_new, block(IconBuilder(context))
        )
    }

    fun plusIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_add, block(IconBuilder(context)))
    }

    fun doneIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_done, block(IconBuilder(context)))
    }

    fun rightArrowIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_arrow_forward, block(IconBuilder(context))
        )
    }

    fun expandMoreIcon(
        context: Context
    ): IconicsDrawable {
        return createIcon(
            context,
            GoogleMaterial.Icon.gmd_expand_more
        )
    }

    fun expandLessIcon(
        context: Context
    ): IconicsDrawable {
        return createIcon(
            context,
            GoogleMaterial.Icon.gmd_expand_less,
        )
    }

    fun leftArrowIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_arrow_back, block(IconBuilder(context)))
    }

    fun lockIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_lock, block(IconBuilder(context)))
    }

    fun closeIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_close, block(IconBuilder(context)))
    }

    fun googleIcon(context: Context): IconicsDrawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_google,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun shareIcon(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_share)
    }

    fun shareIconLight(context: Context): IconicsDrawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_share,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun likeIcon(context: Context): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_trending_up)
    }

    fun commentIcon(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_comment_alt)
    }

    fun moreIcon(context: Context): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_more_vert)
    }

    fun likedIcon(context: Context): IconicsDrawable {
        return createIcon(
            context,
            GoogleMaterial.Icon.gmd_trending_up,
            IconBuilder(context, colorRes = R.color.design_default_color_error)
        )
    }

    fun bookmarkIcon(context: Context): Drawable {
        return createIcon(context, FontAwesome.Icon.faw_bookmark)
    }

    fun bookmarkedIcon(context: Context): Drawable {
        return createIcon(context, FontAwesome.Icon.faw_bookmark1)
    }

    fun boxIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_box_open, block(IconBuilder(context))
        )
    }

    fun dropdownIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_caret_down, block(IconBuilder(context))
        )
    }

    fun signOutIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_sign_out_alt
        )
    }

    @ColorRes
    private fun getIconColor(context: Context): Int {
        return if (isNightMode(context)) android.R.color.white else android.R.color.black
    }

    private fun isNightMode(context: Context): Boolean {
        return context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun urlIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_link
        )
    }

    fun imagesIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_images
        )
    }

    fun quoteLeftIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_quote_left
        )
    }

    fun quoteRightIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_quote_right
        )
    }

    fun saveIconLight(context: Context): Drawable {
        return createIcon(
            context,
            GoogleMaterial.Icon.gmd_save_alt,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun saveIcon(context: Context): Drawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_save_alt
        )
    }

    fun syncIcon(context: Context): Drawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_cloud_upload)
    }

    fun sendIcon(context: Context): Drawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_send)
    }

    fun dramaIcon(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_smile)
    }

    fun levelIcon(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_clock)
    }

    fun noteIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_note, block(IconBuilder(context)))
    }

    fun webIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_web, block(IconBuilder(context)))
    }

    fun imageIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_image, block(IconBuilder(context)))
    }

    fun downloadIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_file_download, block(IconBuilder(context)))
    }

    fun copyIcon(
        context: Context, block: IconBuilder.() -> IconBuilder = { IconBuilder(context) }
    ): IconicsDrawable {
        return createIcon(context, GoogleMaterial.Icon.gmd_content_copy, block(IconBuilder(context)))
    }
}
