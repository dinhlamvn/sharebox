package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyImageModelView
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyTextModelView
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyWebLinkModelView
import com.dinhlam.sharebox.ui.share.ShareState
import com.dinhlam.sharebox.utils.IconUtils
import com.google.gson.Gson

class HomeShareListModelViewsBuilder constructor(
    private val activity: HomeActivity,
    private val viewModel: HomeViewModel,
    private val gson: Gson
) : () -> List<BaseListAdapter.BaseModelView> {

    override fun invoke(): List<BaseListAdapter.BaseModelView> {
        return mutableListOf<BaseListAdapter.BaseModelView>().apply {
            activity.getState(viewModel) { state ->
                if (state.isRefreshing) {
                    add(LoadingModelView)
                    return@getState
                }

                addAll(buildShares(state.shareList))
            }
        }
    }

    private fun buildShares(shares: List<Share>): List<BaseListAdapter.BaseModelView> {
        return shares.mapNotNull { share ->
            when (share.shareType) {
                ShareType.WEB.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo,
                        ShareState.ShareInfo.ShareWebLink::class.java
                    )
                    ShareRecentlyWebLinkModelView(
                        id = "${share.id}",
                        iconUrl = IconUtils.getIconUrl(shareInfo.url),
                        url = shareInfo.url,
                        createdAt = share.createdAt,
                        note = share.shareNote,
                        shareId = share.id
                    )
                }
                ShareType.IMAGE.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo,
                        ShareState.ShareInfo.ShareImage::class.java
                    )
                    ShareRecentlyImageModelView(
                        "${share.id}",
                        shareInfo.uri,
                        share.createdAt,
                        share.shareNote,
                        share.id
                    )
                }
                ShareType.TEXT.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo,
                        ShareState.ShareInfo.ShareText::class.java
                    )
                    ShareRecentlyTextModelView(
                        id = "${share.id}",
                        iconUrl = IconUtils.getIconUrl(shareInfo.text),
                        content = shareInfo.text,
                        createdAt = share.createdAt,
                        note = share.shareNote,
                        shareId = share.id
                    )
                }
                else -> {
                    null
                }
            }
        }
    }
}
