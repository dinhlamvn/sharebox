package com.dinhlam.sharebox.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.imageloader.config.ImageLoadConfig
import com.google.firebase.storage.StorageReference
import java.io.File

abstract class ImageLoader {

    companion object {
        lateinit var INSTANCE: ImageLoader
            private set

        private val lock = Any()

        @Synchronized
        fun setLoader(loader: ImageLoader) {
            synchronized(lock) {
                this.INSTANCE = loader
            }
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

    abstract fun load(
        context: Context,
        storageReference: StorageReference,
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
