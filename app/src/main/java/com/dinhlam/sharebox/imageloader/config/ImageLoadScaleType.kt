package com.dinhlam.sharebox.imageloader.config

sealed interface ImageLoadScaleType {
    object None : ImageLoadScaleType
    object CenterCrop : ImageLoadScaleType
    object FitCenter : ImageLoadScaleType
}