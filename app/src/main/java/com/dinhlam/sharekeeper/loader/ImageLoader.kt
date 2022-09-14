package com.dinhlam.sharekeeper.loader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
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

    fun get(context: Context, uri: Uri?): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit().get()
    }
}