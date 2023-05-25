package com.dinhlam.sharebox.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.imageloader.config.ImageLoadConfig
import java.io.File

abstract class ImageLoader {

    companion object {
        lateinit var instance: ImageLoader
            private set

        @Synchronized
        fun setLoader(loader: ImageLoader) {
            this.instance = loader
        }
    }

    abstract fun load(
        context: Context,
        @DrawableRes drawable: Int,
        iv: ImageView,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    )

    abstract fun load(
        context: Context,
        url: String?,
        iv: ImageView,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    )

    abstract fun load(
        context: Context,
        uri: Uri?,
        iv: ImageView,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    )

    abstract fun load(
        context: Context,
        file: File?,
        iv: ImageView,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    )

    abstract fun get(
        context: Context,
        model: Any?,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    ): Bitmap?

    abstract fun release(context: Context, iv: ImageView)
}
