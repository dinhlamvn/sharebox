package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.data.model.UserDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareToShareDetailMapper @Inject constructor() {

    fun map(
        share: Share,
        user: UserDetail,
        commentNumber: Int,
        likeNumber: Int,
        bookmarked: Boolean,
        liked: Boolean
    ): ShareDetail {
        return ShareDetail(
            share.shareId,
            user,
            share.shareNote,
            share.shareDate,
            share.createdAt,
            share.shareData,
            commentNumber,
            likeNumber,
            bookmarked,
            liked
        )
    }
}