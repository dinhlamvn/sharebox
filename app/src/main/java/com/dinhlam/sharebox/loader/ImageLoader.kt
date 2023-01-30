package com.dinhlam.sharebox.loader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.dinhlam.sharebox.R

object ImageLoader {

    fun load(
        context: Context,
        url: String?,
        imageView: ImageView,
        @DrawableRes error: Int = R.drawable.no_image
    ) {
        Glide.with(context)
            .load(url)
            .error(error)
            .into(imageView)
    }

    fun load(context: Context, uri: Uri?, imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .fitCenter()
            .into(imageView)
    }

    fun load(
        context: Context,
        @DrawableRes res: Int,
        imageView: ImageView,
        circle: Boolean = false,
        @DrawableRes error: Int = R.drawable.no_image
    ) {
        var req = Glide.with(context)
            .load(res)
        if (circle) req = req.circleCrop()
        req.error(error).into(imageView)
    }

    fun load(
        context: Context,
        url: String?,
        imageView: ImageView,
        @DrawableRes error: Int = 0,
        circle: Boolean = false
    ) {
        var req = Glide.with(context)
            .load(url)
        if (circle) req = req.circleCrop()
        if (error != 0) req = req.error(error)
        req.into(imageView)
    }

    fun get(context: Context, uri: Uri?): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit().get()
    }
}
