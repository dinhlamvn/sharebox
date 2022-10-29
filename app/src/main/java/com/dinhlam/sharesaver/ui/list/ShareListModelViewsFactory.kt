package com.dinhlam.sharesaver.ui.list

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.database.entity.Share
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeTextModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.ui.share.ShareState
import com.dinhlam.sharesaver.utils.IconUtils
import com.google.gson.Gson

class ShareListModelViewsFactory constructor(
    private val activity: ShareListActivity,
    private val viewModel: ShareListViewModel,
    private val gson: Gson
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = activity.withData(viewModel) { data ->
        if (data.isRefreshing) {
            LoadingModelView.addTo(this)
            return@withData
        }

        val map = data.shareList.groupBy { it.createdAt.format("yyyy-MM-dd") }
        map.forEach { entry ->
            val date = entry.key
            val shares = entry.value
            HomeDateModelView("date$date", date).addTo(this)
            buildShares(shares)
        }
    }

    private fun buildShares(shares: List<Share>) {
        shares.mapNotNull { share ->
            when (share.shareType) {
                "web-link" -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareWebLink::class.java
                    )
                    HomeWebLinkModelView(
                        id = "${share.id}",
                        iconUrl = IconUtils.getIconUrl(shareInfo.url),
                        url = shareInfo.url,
                        createdAt = share.createdAt,
                        note = share.shareNote,
                        shareId = share.id
                    )
                }
                "image" -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareImage::class.java
                    )
                    HomeImageModelView(
                        "${share.id}", shareInfo.uri, share.createdAt, share.shareNote, share.id
                    )
                }
                "text" -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareText::class.java
                    )
                    HomeTextModelView(
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
        }.forEach { it.addTo(this) }
    }
}