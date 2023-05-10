package com.dinhlam.sharebox.loader

import android.app.Activity
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
        @DrawableRes error: Int = R.drawable.no_preview_image
    ) {
        if (context is Activity && context.isFinishing) {
            return
        }

        Glide.with(context)
            .load(url)
            .error(error)
            .centerCrop()
            .into(imageView)
    }

    fun load(context: Context, uri: Uri?, imageView: ImageView) {
        if (context is Activity && context.isFinishing) {
            return
        }

        Glide.with(context)
            .load(uri)
            .centerCrop()
            .into(imageView)
    }

    fun load(
        context: Context,
        @DrawableRes res: Int,
        imageView: ImageView,
        circle: Boolean = false,
        @DrawableRes error: Int = R.drawable.ic_image_broken
    ) {
        if (context is Activity && context.isFinishing) {
            return
        }

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
        if (context is Activity && context.isFinishing) {
            return
        }

        var req = Glide.with(context)
            .load(url)
            .placeholder(R.drawable.no_preview_image)
        if (circle) req = req.circleCrop()
        if (error != 0) req = req.error(error)
        req.into(imageView)
    }

    fun load(
        context: Context,
        drawable: Int,
        imageView: ImageView,
        @DrawableRes error: Int = 0,
        circle: Boolean = false
    ) {
        if (context is Activity && context.isFinishing) {
            return
        }

        var req = Glide.with(context)
            .load(drawable)
            .placeholder(R.drawable.no_preview_image)
        if (circle) req = req.circleCrop()
        if (error != 0) req = req.error(error)
        req.into(imageView)
    }

    fun get(context: Context, uri: Uri?): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit().get()
    }
}
