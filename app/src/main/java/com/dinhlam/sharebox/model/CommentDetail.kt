package com.dinhlam.sharebox.model

data class CommentDetail(
    val id: Int,
    val shareId: String,
    val content: String?,
    val commentDate: Long,
    val createdAt: Long,
    val userDetail: UserDetail
)