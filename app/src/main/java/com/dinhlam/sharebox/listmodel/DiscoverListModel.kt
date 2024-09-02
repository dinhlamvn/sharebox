package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelDiscoverBinding
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.model.Spacing

data class DiscoverListModel(
    val models: List<BaseListAdapter.BaseListModel>,
    val margin: Spacing
) : BaseListAdapter.BaseListModel("discover") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return DiscoverViewHolder(inflater, container)
    }

    private class DiscoverViewHolder(layoutInflater: LayoutInflater, container: ViewGroup) :
        BaseListAdapter.BaseViewHolder<DiscoverListModel, ListModelDiscoverBinding>(
            ListModelDiscoverBinding.inflate(layoutInflater, container, false)
        ) {
        private val adapter = BaseListAdapter.create()

        init {
            adapter.attachTo(binding.recyclerView, buildContext.castNonNull())
        }

        override fun onBind(model: DiscoverListModel, position: Int) {
            binding.root.updateMargin(model.margin)
            adapter.requestBuildListModels(model.models)
        }

        override fun onUnBind() {

        }
    }
}