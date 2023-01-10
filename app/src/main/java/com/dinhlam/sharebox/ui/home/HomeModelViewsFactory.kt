package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.modelview.FolderListModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.utils.TagUtil
import com.google.gson.Gson

class HomeModelViewsFactory(
    private val homeActivity: HomeActivity,
    private val viewModel: HomeViewModel,
    private val gson: Gson
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = homeActivity.getState(viewModel) { state ->
        if (state.isRefreshing) {
            LoadingModelView.addTo(this)
            return@getState
        }
        state.folders.forEach { folder ->
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
