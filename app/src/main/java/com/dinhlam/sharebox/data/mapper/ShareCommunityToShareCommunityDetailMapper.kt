package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.ShareCommunity
import com.dinhlam.sharebox.data.model.ShareCommunityDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareCommunityToShareCommunityDetailMapper @Inject constructor() {

    fun map(shareCommunity: ShareCommunity): ShareCommunityDetail {
        return ShareCommunityDetail(
            shareCommunity.id,
            shareCommunity.shareId,
            shareCommunity.sharePower,
            shareCommunity.createdAt
        )
    }
}