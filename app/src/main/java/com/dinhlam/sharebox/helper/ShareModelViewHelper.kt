package com.dinhlam.sharebox.helper

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.ui.share.ShareState
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ShareModelViewHelper @Inject constructor(
    private val gson: Gson
) {

    fun buildShareModelViews(shares: List<Share>): List<BaseListAdapter.BaseModelView> {
        return shares.mapNotNull { share ->
            when (share.shareType) {
                ShareType.WEB.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareWebLink::class.java
                    )
                    shareInfo.buildShareModelViews(share.id, share.createdAt, share.shareNote)
                }

                ShareType.IMAGE.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareImage::class.java
                    )
                    shareInfo.buildShareModelViews(share.id, share.createdAt, share.shareNote)
                }

                ShareType.TEXT.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareText::class.java
                    )
                    shareInfo.buildShareModelViews(share.id, share.createdAt, share.shareNote)
                }

                else -> {
                    null
                }
            }
        }
    }
}