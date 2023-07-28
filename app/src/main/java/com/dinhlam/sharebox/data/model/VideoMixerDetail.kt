package com.dinhlam.sharebox.data.model

data class VideoMixerDetail(
    val id: Int,
    val shareId: String,
    val videoSource: VideoSource,
    val originUrl: String,
    val source: VideoSource,
    val sourceId: String,
    val shareDetail: ShareDetail,
)
