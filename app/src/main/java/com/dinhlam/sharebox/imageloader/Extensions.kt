package com.dinhlam.sharebox.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.imageloader.config.ImageLoadConfig
import com.google.firebase.storage.StorageReference
import java.io.File

fun ImageView.load(
    context: Context,
    @DrawableRes drawable: Int,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
) {
    ImageLoader.INSTANCE.load(context, drawable, this, block)
}

fun ImageView.load(
    context: Context,
    url: String?,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
) {
    ImageLoader.INSTANCE.load(context, url, this, block)
}

fun ImageView.load(
    context: Context,
    uri: Uri?,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
) {
    ImageLoader.INSTANCE.load(context, uri, this, block)
}

fun ImageView.load(
    context: Context,
    file: File?,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
) {
    ImageLoader.INSTANCE.load(context, file, this, block)
}

fun ImageView.load(
    context: Context,
    storageReference: StorageReference,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
) {
    ImageLoader.INSTANCE.load(context, storageReference, this, block)
}

fun Any?.get(
    context: Context,
    block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
): Bitmap? {
    return ImageLoader.INSTANCE.get(context, this, block)
}

fun ImageView.release(context: Context) = ImageLoader.INSTANCE.release(context, this)