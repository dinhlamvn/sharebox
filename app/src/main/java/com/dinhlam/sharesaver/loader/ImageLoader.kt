package com.dinhlam.sharesaver.loader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

object ImageLoader {

    fun load(context: Context, url: String?, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .into(imageView)
    }

    fun load(context: Context, uri: Uri?, imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .into(imageView)
    }

    fun load(
        context: Context,
        @DrawableRes res: Int,
        imageView: ImageView,
        circle: Boolean = false
    ) {
        var req = Glide.with(context)
            .load(res)
        if (circle) req = req.circleCrop()
        req.into(imageView)
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