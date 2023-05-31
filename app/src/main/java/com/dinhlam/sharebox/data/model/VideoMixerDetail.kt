package com.dinhlam.sharebox.data.model

data class VideoMixerDetail(
    val id: Int,
    val shareId: String,
    val originUrl: String,
    val source: VideoSource,
    val sourceId: String,
    val uri: String?,
    val shareDetail: ShareDetail,
)