package com.dinhlam.sharebox.model

data class ShareDetail(
    val shareId: String,
    val user: UserDetail,
    val shareNote: String?,
    val createdAt: Long,
    val shareData: ShareData
)
