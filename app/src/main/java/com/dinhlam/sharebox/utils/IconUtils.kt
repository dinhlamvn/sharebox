package com.dinhlam.sharebox.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.dinhlam.sharebox.R
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp

object IconUtils {

    fun downloadIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_download
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun googleIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_google
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun shareIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_share
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun shareIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_share
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun likeIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun likeIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun commentIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_comment
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun commentIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_comment
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun likedIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart1
        ).apply {
            sizeDp = 24
            colorRes = R.color.design_default_color_error
        }
    }

    fun bookmarkIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun bookmarkedIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark1
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun bookmarkIconLight(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun bookmarkedIconLight(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark1
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.white
        }
    }

    fun boxIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_box
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }

    fun signOutIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_sign_out_alt
        ).apply {
            sizeDp = 24
            colorRes = android.R.color.black
        }
    }
}
