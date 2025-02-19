package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShareDetail(
    val id: Int,
    val shareId: String,
    val shareNote: String?,
    val shareDate: Long,
    val createdAt: Long,
    val shareData: ShareData,
    val commentNumber: Int,
    val likeNumber: Int,
    val liked: Boolean,
    val commentDetail: CommentDetail?,
    val boxDetail: BoxDetail?,
    val isVideoShare: Boolean,
    val tagId: Int?,
    val tagColor: Int?
) : Parcelable
