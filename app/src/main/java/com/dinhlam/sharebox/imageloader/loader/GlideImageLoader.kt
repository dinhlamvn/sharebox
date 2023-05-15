package com.dinhlam.sharebox.imageloader.loader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadConfig
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import java.io.File

object GlideImageLoader : ImageLoader() {

    private fun isValidContext(context: Context): Boolean {
        if (context is Application) {
            return true
        }

        val activity = context.cast<Activity>()

        if (activity?.isFinishing == true) {
            return false
        }

        return true
    }

    private fun toActivityContext(context: Context): Activity? {
        if (context is ContextWrapper) {
            val ctw: ContextWrapper = context.castNonNull()
            return ctw.cast()
        }
        return context.cast()
    }

    private fun createScaleTypeTransform(
        transforms: MutableList<BitmapTransformation>, scaleType: ImageLoadScaleType
    ) {
        if (scaleType is ImageLoadScaleType.CenterCrop) {
            transforms.add(CenterCrop())
        } else if (scaleType is ImageLoadScaleType.FitCenter) {
            transforms.add(FitCenter())
        }
    }

    private fun createTransformRequest(
        builder: RequestBuilder<*>, transformType: TransformType
    ): RequestBuilder<*> = builder.run {
        when (transformType) {
            is TransformType.Rounded -> {
                val transform = transformType.castNonNull<TransformType.Rounded>()
                val transforms =
                    mutableListOf<BitmapTransformation>(RoundedCorners(transform.radius))
                createScaleTypeTransform(transforms, transform.scaleType)
                transform(*transforms.toTypedArray())
            }

            is TransformType.Circle -> {
                val transform = transformType.castNonNull<TransformType.Circle>()
                val transforms = mutableListOf<BitmapTransformation>(CircleCrop())
                createScaleTypeTransform(transforms, transform.scaleType)
                transform(*transforms.toTypedArray())
            }

            is TransformType.Normal -> {
                val transform = transformType.castNonNull<TransformType.Normal>()
                val transforms = mutableListOf<BitmapTransformation>()
                createScaleTypeTransform(transforms, transform.scaleType)
                transform(*transforms.toTypedArray())
            }

            else -> this
        }
    }

    private fun buildRequest(
        context: Context, any: Any?, block: ImageLoadConfig.() -> ImageLoadConfig
    ): RequestBuilder<*>? {
        val toContext = toActivityContext(context) ?: context.applicationContext

        if (!isValidContext(context)) {
            return null
        }

        val config = block.invoke(ImageLoadConfig())

        return buildRequest(Glide.with(toContext).load(any), config)
    }

    private fun buildRequest(
        builder: RequestBuilder<*>, config: ImageLoadConfig
    ): RequestBuilder<*> = builder.run {
        createTransformRequest(this, config.transformType)
    }

    override fun load(
        context: Context, drawable: Int, iv: ImageView, block: ImageLoadConfig.() -> ImageLoadConfig
    ) {
        buildRequest(context, drawable, block)?.into(iv)
    }

    override fun load(
        context: Context, url: String?, iv: ImageView, block: ImageLoadConfig.() -> ImageLoadConfig
    ) {
        buildRequest(context, url, block)?.into(iv)
    }

    override fun load(
        context: Context, uri: Uri?, iv: ImageView, block: ImageLoadConfig.() -> ImageLoadConfig
    ) {
        buildRequest(context, uri, block)?.into(iv)
    }

    override fun load(
        context: Context, file: File?, iv: ImageView, block: ImageLoadConfig.() -> ImageLoadConfig
    ) {
        buildRequest(context, file, block)?.into(iv)
    }

    override fun get(
        context: Context, model: Any?, block: ImageLoadConfig.() -> ImageLoadConfig
    ): Bitmap? {
        val toContext = toActivityContext(context) ?: context.applicationContext

        if (!isValidContext(context)) {
            return null
        }

        val config = block.invoke(ImageLoadConfig())

        return buildRequest(Glide.with(toContext).asBitmap().load(model), config).submit().get()
            .cast()
    }
}