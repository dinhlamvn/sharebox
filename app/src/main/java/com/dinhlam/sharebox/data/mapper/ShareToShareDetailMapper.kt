package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.CommentDetail
import com.dinhlam.sharebox.model.ShareDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareToShareDetailMapper @Inject constructor() {

    fun map(
        share: Share,
        commentNumber: Int = 0,
        likeNumber: Int = 0,
        liked: Boolean = false,
        commentDetail: CommentDetail? = null,
        boxDetail: BoxDetail? = null,
        isVideoShare: Boolean = false,
        tagId: Int? = null,
        tagColor: Int? = null,
    ): ShareDetail {
        return ShareDetail(
            share.id,
            share.shareId,
            share.shareNote,
            share.shareDate,
            share.createdAt,
            share.shareData,
            commentNumber,
            likeNumber,
            liked,
            commentDetail,
            boxDetail,
            isVideoShare,
            tagId,
            tagColor,
        )
    }
}