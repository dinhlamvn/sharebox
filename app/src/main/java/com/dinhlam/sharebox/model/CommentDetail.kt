package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentDetail(
    val id: Int,
    val shareId: String,
    val content: String?,
    val commentDate: Long,
    val createdAt: Long,
    val userDetail: UserDetail
) : Parcelable