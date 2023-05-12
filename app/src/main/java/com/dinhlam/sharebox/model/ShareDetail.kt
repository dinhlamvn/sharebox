package com.dinhlam.sharebox.model

data class ShareDetail(
    val id: Int,
    val user: UserDetail,
    val shareNote: String?,
    val createdAt: Long,
    val shareData: ShareData
)
