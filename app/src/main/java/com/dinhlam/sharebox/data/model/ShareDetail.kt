package com.dinhlam.sharebox.data.model

data class ShareDetail(
    val shareId: String,
    val user: UserDetail,
    val shareNote: String?,
    val createdAt: Long,
    val shareData: ShareData,
    val commentCount: Int
)
