package com.dinhlam.sharebox.imageloader.config

import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.R

data class ImageLoadConfig(
    val transformType: TransformType = TransformType.Normal(),
    @DrawableRes val errorDrawable: Int = R.drawable.no_preview_image
)