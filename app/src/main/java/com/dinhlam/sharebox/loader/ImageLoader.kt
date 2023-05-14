package com.dinhlam.sharebox.loader

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.castNonNull

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

        Glide.with(context).load(url).error(error).centerCrop().into(imageView)
    }

    fun load(
        context: Context,
        uri: Uri?,
        imageView: ImageView,
        block: ImageLoadConfig.() -> ImageLoadConfig = { ImageLoadConfig() }
    ) {
        if (context is Activity && context.isFinishing) {
            return
        }

        val config = block.invoke(ImageLoadConfig())

        Glide.with(context).load(uri).run {
            createTransformRequest(this, config.transformType)
        }.into(imageView)
    }

    private fun createScaleTypeRequest(
        builder: RequestBuilder<Drawable>, scaleType: ImageLoadScaleType
    ): RequestBuilder<Drawable> = builder.run {
        if (scaleType == ImageLoadScaleType.CenterCrop) {
            centerCrop()
        } else {
            fitCenter()
        }
    }

    private fun createTransformRequest(
        builder: RequestBuilder<Drawable>, transformType: TransformType
    ): RequestBuilder<Drawable> = builder.run {
        when (transformType) {
            is TransformType.Rounded -> {
                val roundedTransform = transformType.castNonNull<TransformType.Rounded>()
                val transforms =
                    mutableListOf<BitmapTransformation>(RoundedCorners(roundedTransform.radius))
                if (roundedTransform.scaleType == ImageLoadScaleType.CenterCrop) {
                    transforms.add(CenterCrop())
                } else if (roundedTransform.scaleType == ImageLoadScaleType.FitCenter) {
                    transforms.add(FitCenter())
                }
                transform(*transforms.toTypedArray())
            }

            is TransformType.Circle -> {
                val roundedTransform = transformType.castNonNull<TransformType.Circle>()
                val transforms =
                    mutableListOf<BitmapTransformation>(CircleCrop())
                if (roundedTransform.scaleType == ImageLoadScaleType.CenterCrop) {
                    transforms.add(CenterCrop())
                } else if (roundedTransform.scaleType == ImageLoadScaleType.FitCenter) {
                    transforms.add(FitCenter())
                }
                transform(*transforms.toTypedArray())
            }

            else -> this
        }
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

        var req = Glide.with(context).load(res)
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

        var req = Glide.with(context).load(url).placeholder(R.drawable.no_preview_image)
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

        var req = Glide.with(context).load(drawable).placeholder(R.drawable.no_preview_image)
        if (circle) req = req.circleCrop()
        if (error != 0) req = req.error(error)
        req.into(imageView)
    }

    fun get(context: Context, uri: Uri?): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit().get()
    }

    sealed interface ImageLoadScaleType {
        object None : ImageLoadScaleType
        object CenterCrop : ImageLoadScaleType
        object FitCenter : ImageLoadScaleType
    }

    sealed class TransformType(scaleType: ImageLoadScaleType) {
        object None : TransformType(ImageLoadScaleType.None)
        class Rounded(val radius: Int, val scaleType: ImageLoadScaleType) : TransformType(scaleType)
        class Circle(val scaleType: ImageLoadScaleType) : TransformType(scaleType)
    }

    data class ImageLoadConfig(
        val transformType: TransformType = TransformType.None
    )
}
