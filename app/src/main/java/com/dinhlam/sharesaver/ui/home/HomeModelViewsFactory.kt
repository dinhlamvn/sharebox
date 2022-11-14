package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.modelview.FolderListModelView
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.utils.TagUtil
import com.google.gson.Gson

class HomeModelViewsFactory(
    private val homeActivity: HomeActivity,
    private val viewModel: HomeViewModel,
    private val gson: Gson
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = homeActivity.withState(viewModel) { data ->
        if (data.isRefreshing) {
            LoadingModelView.addTo(this)
            return@withState
        }
        data.folders.forEach { folder ->
            FolderListModelView(
                "folder_${folder.id}",
                folder.name,
                folder.desc,
                folder.updatedAt,
                !folder.password.isNullOrEmpty(),
                TagUtil.getTag(folder.tag)
            ).addTo(this)
        }
    }
}