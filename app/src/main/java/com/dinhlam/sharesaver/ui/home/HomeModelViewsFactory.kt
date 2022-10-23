package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.modelview.FolderListModelView
import com.dinhlam.sharesaver.modelview.LoadingModelView
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
        data.folders.map { folder ->
            FolderListModelView(
                "folder_${folder.id}",
                folder.name,
                folder.desc,
                folder.updatedAt,
                !folder.password.isNullOrEmpty()
            )
        }.forEach { it.addTo(this) }
    }
}