package com.dinhlam.sharebox.data.model

data class ShareDetail(
    val shareId: String,
    val user: UserDetail,
    val shareNote: String?,
    val shareDate: Long,
    val createdAt: Long,
    val shareData: ShareData,
    val commentNumber: Int,
    val likeNumber: Int,
    val bookmarked: Boolean,
    val liked: Boolean
)
