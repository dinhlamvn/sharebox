package com.dinhlam.sharebox.data.model

data class CommentDetail(
    val id: Int,
    val shareId: String,
    val content: String?,
    val createdAt: Long,
    val userDetail: UserDetail
)