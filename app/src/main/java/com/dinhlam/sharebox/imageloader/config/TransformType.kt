package com.dinhlam.sharebox.imageloader.config

sealed class TransformType {
    class Normal(val scaleType: ImageLoadScaleType = ImageLoadScaleType.None) :
        TransformType()

    class Rounded(val radius: Int, val scaleType: ImageLoadScaleType) : TransformType()
    class Circle(val scaleType: ImageLoadScaleType) : TransformType()
}