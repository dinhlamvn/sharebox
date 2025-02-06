package com.dinhlam.sharebox.model

sealed class VideoSource(val sourceName: String) {
    data object Directly : VideoSource("directly")
    data object Youtube : VideoSource("youtube")
    data object Tiktok : VideoSource("tiktok")
    data object Facebook : VideoSource("facebook")
}