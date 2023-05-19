package com.dinhlam.sharebox.imageloader.config

import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.R

data class ImageLoadConfig(
    val transformType: TransformType = TransformType.Normal(),
    @DrawableRes val thumbnailDrawable: Int = R.drawable.image_loading_placeholder,
    @DrawableRes val errorDrawable: Int = R.drawable.image_no_preview
)