package com.dinhlam.keepmyshare.loader

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

object ImageLoader {

    fun load(context: Context, url: String?, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .into(imageView)
    }
}