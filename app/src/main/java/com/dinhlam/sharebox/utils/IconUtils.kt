package com.dinhlam.sharebox.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.dinhlam.sharebox.R
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp

object IconUtils {

    fun shareIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_share
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextBlack
        }
    }

    fun shareIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_share
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorWhite
        }
    }

    fun likeIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextBlack
        }
    }

    fun likeIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorWhite
        }
    }

    fun commentIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_comment
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextBlack
        }
    }

    fun commentIconLight(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_comment
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorWhite
        }
    }

    fun likedIcon(context: Context): IconicsDrawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_heart1
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextRed
        }
    }

    fun bookmarkIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextBlack
        }
    }

    fun bookmarkedIcon(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark1
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorTextBlack
        }
    }

    fun bookmarkIconLight(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorWhite
        }
    }

    fun bookmarkedIconLight(context: Context): Drawable {
        return IconicsDrawable(
            context,
            FontAwesome.Icon.faw_bookmark1
        ).apply {
            sizeDp = 24
            colorRes = R.color.colorWhite
        }
    }
}
