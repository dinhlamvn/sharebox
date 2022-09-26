package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.modelview.FolderModelView
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeTextModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.ui.share.ShareData
import com.dinhlam.sharesaver.utils.IconUtils
import com.google.gson.Gson

class HomeModelViewsFactory(
    private val homeActivity: HomeActivity,
    private val viewModel: HomeViewModel,
    private val gson: Gson
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = homeActivity.withData(viewModel) { data ->
        if (data.isRefreshing) {
            LoadingModelView.addTo(this)
            return@withData
        }

        if (data.selectedFolder == null) {
            data.folders.map { folder ->
                FolderModelView(
                    "folder_${folder.id}",
                    folder.name,
                    folder.desc,
                    !folder.password.isNullOrEmpty()
                )
            }.forEach { it.addTo(this) }
            return@withData
        }
        data.shareList.groupBy { it.createdAt.format("yyyy-MM-dd") }.forEach { entry ->
            val date = entry.key
            val shares = entry.value
            HomeDateModelView("date$date", date).addTo(this)
            shares.mapNotNull { share ->
                when (share.shareType) {
                    "web-link" -> {
                        val shareInfo = gson.fromJson(
                            share.shareInfo, ShareData.ShareInfo.ShareWebLink::class.java
                        )
                        HomeWebLinkModelView(
                            id = "${share.id}",
                            iconUrl = IconUtils.getIconUrl(shareInfo.url),
                            url = shareInfo.url,
                            createdAt = share.createdAt,
                            note = share.shareNote
                        )
                    }
                    "image" -> {
                        val shareInfo = gson.fromJson(
                            share.shareInfo, ShareData.ShareInfo.ShareImage::class.java
                        )
                        HomeImageModelView(
                            "${share.id}", shareInfo.uri, share.createdAt, share.shareNote
                        )
                    }
                    "text" -> {
                        val shareInfo = gson.fromJson(
                            share.shareInfo, ShareData.ShareInfo.ShareText::class.java
                        )
                        HomeTextModelView(
                            id = "${share.id}",
                            iconUrl = IconUtils.getIconUrl(shareInfo.text),
                            content = shareInfo.text,
                            createdAt = share.createdAt,
                            note = share.shareNote
                        )
                    }
                    else -> {
                        null
                    }
                }
            }.forEach { it.addTo(this) }
        }
    }
}