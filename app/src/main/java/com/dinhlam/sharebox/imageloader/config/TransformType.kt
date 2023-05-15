package com.dinhlam.sharebox.imageloader.config

sealed class TransformType(scaleType: ImageLoadScaleType) {
    class Normal(val scaleType: ImageLoadScaleType = ImageLoadScaleType.None) :
        TransformType(scaleType)

    class Rounded(val radius: Int, val scaleType: ImageLoadScaleType) : TransformType(scaleType)
    class Circle(val scaleType: ImageLoadScaleType) : TransformType(scaleType)
}