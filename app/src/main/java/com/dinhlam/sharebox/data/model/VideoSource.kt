package com.dinhlam.sharebox.data.model

sealed class VideoSource(val sourceName: String) {
    object Youtube : VideoSource("youtube")
    object Tiktok : VideoSource("tiktok")
    object Facebook : VideoSource("facebook")
}