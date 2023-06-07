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

object IconUtils {

    data class IconBuilder(
        val context: Context,
        val sizeDp: Int = 24, @ColorRes val colorRes: Int = 0
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

    fun downloadIconLight(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_download)
    }

    fun googleIconLight(context: Context): IconicsDrawable {
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
        return createIcon(context, FontAwesome.Icon.faw_heart)
    }

    fun likeIconLight(context: Context): IconicsDrawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_heart,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun commentIcon(context: Context): IconicsDrawable {
        return createIcon(context, FontAwesome.Icon.faw_comment)
    }

    fun commentIconLight(context: Context): IconicsDrawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_comment,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun likedIcon(context: Context): IconicsDrawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_heart1,
            IconBuilder(context, colorRes = R.color.design_default_color_error)
        )
    }

    fun bookmarkIcon(context: Context): Drawable {
        return createIcon(context, FontAwesome.Icon.faw_bookmark)
    }

    fun bookmarkedIcon(context: Context): Drawable {
        return createIcon(context, FontAwesome.Icon.faw_bookmark1)
    }

    fun bookmarkIconLight(context: Context): Drawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_bookmark,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun bookmarkedIconLight(context: Context): Drawable {
        return createIcon(
            context,
            FontAwesome.Icon.faw_bookmark1,
            IconBuilder(context, colorRes = android.R.color.white)
        )
    }

    fun boxIcon(context: Context): Drawable {
        return createIcon(
            context, FontAwesome.Icon.faw_box
        )
    }

    fun signOutIcon(context: Context): Drawable {
        return createIcon(
            context, GoogleMaterial.Icon.gmd_logout
        )
    }

    @ColorRes
    private fun getIconColor(context: Context): Int {
        return if (isNightMode(context)) android.R.color.white else android.R.color.black
    }

    private fun isNightMode(context: Context): Boolean {
        return context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
