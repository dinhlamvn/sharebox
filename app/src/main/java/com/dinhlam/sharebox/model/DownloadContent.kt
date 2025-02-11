package com.dinhlam.sharebox.model

data class DownloadContent(
    val videos: List<DownloadData> = emptyList(),
    val audios: List<DownloadData> = emptyList(),
    val images: List<DownloadData> = emptyList(),
    val files: List<DownloadData> = emptyList(),
)