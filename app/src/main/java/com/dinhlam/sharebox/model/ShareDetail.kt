package com.dinhlam.sharebox.model

data class ShareDetail(
    val id: Int,
    val userId: String,
    val shareNote: String,
    val createdAt: Long,
    val shareData: ShareData
)
