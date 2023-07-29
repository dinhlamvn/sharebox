package com.dinhlam.sharebox.model

data class ShareDetail(
    val id: Int,
    val shareId: String,
    val user: UserDetail,
    val shareNote: String?,
    val shareDate: Long,
    val createdAt: Long,
    val shareData: ShareData,
    val commentNumber: Int,
    val likeNumber: Int,
    val bookmarked: Boolean,
    val liked: Boolean,
    val commentDetail: CommentDetail?,
    val boxDetail: BoxDetail?,
    val isVideoShare: Boolean,
)
