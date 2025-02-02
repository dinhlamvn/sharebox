package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelDiscoverBinding
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.model.Spacing

data class DiscoverListModel(
    val models: List<BaseListAdapter.BaseListModel>,
    val margin: Spacing
) : BaseListAdapter.BaseListModel("discover") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return DiscoverViewHolderViewBinding(inflater, container)
    }

    private class DiscoverViewHolderViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup
    ) :
        BaseListAdapter.BaseViewHolderViewBinding<DiscoverListModel, ListModelDiscoverBinding>(
            ListModelDiscoverBinding.inflate(layoutInflater, container, false)
        ) {
        private val adapter = BaseListAdapter.create()

        init {
            binding.recyclerView.itemAnimator = null
            adapter.attachTo(binding.recyclerView)
        }

        override fun onBind(model: DiscoverListModel, position: Int) {
            binding.root.updateMargin(model.margin)
            adapter.requestBuildListModels(model.models)
        }

        override fun onUnBind() {

        }
    }
}