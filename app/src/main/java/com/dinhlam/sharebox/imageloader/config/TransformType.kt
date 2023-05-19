package com.dinhlam.sharebox.imageloader.config

sealed interface TransformType {
    class Normal(val scaleType: ImageLoadScaleType = ImageLoadScaleType.None) :
        TransformType

    class Rounded(val radius: Int, val scaleType: ImageLoadScaleType) : TransformType
    class Circle(val scaleType: ImageLoadScaleType) : TransformType
}