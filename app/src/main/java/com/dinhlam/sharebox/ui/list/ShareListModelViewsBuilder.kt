package com.dinhlam.sharebox.ui.list

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SingleTextModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListDateModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListImageModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListTextModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListWebLinkModelView
import com.dinhlam.sharebox.ui.share.ShareState
import com.dinhlam.sharebox.utils.IconUtils
import com.google.gson.Gson

class ShareListModelViewsBuilder constructor(
    private val activity: ShareListActivity,
    private val viewModel: ShareListViewModel,
    private val gson: Gson
) : (MutableList<BaseListAdapter.BaseModelView>) -> Unit {

    override fun invoke(list: MutableList<BaseListAdapter.BaseModelView>) {
        activity.getState(viewModel) { state ->
            if (state.isRefreshing) {
                list.add(LoadingModelView)
                return@getState
            }

            if (state.shareList.isEmpty()) {
                list.add(SingleTextModelView(activity.getString(R.string.no_result)))
                return@getState
            }

            val map = state.shareList.groupBy { it.createdAt.format("yyyy-MM-dd") }
            map.forEach { entry ->
                val date = entry.key
                val shares = entry.value
                list.add(ShareListDateModelView("date$date", date))
                list.addAll(buildShares(shares))
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
                    ShareListWebLinkModelView(
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
                    ShareListImageModelView(
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
                    ShareListTextModelView(
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
