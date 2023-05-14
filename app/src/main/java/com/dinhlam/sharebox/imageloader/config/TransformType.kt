package com.dinhlam.sharebox.imageloader.config

sealed class TransformType(scaleType: ImageLoadScaleType) {
    object None : TransformType(ImageLoadScaleType.None)
    class Rounded(val radius: Int, val scaleType: ImageLoadScaleType) : TransformType(scaleType)
    class Circle(val scaleType: ImageLoadScaleType) : TransformType(scaleType)
}