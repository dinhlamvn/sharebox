package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelZingNewsDiscoverBinding

data class ZingNewsDiscoverListModel(
    val id: String,
    val url: String,
    val title: String,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onArchiveClick: BaseListAdapter.NoHashProp<OnClickListener>,
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ZingNewsDiscoverListModel, ListModelZingNewsDiscoverBinding>(
                ListModelZingNewsDiscoverBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ZingNewsDiscoverListModel, position: Int) {
                binding.root.setOnClickListener(model.onClick.prop)
                binding.buttonArchive.setOnClickListener(model.onArchiveClick.prop)
                binding.shareLinkPreview.setLink(model.url)
                binding.textDesc.text = model.title
            }

            override fun onUnBind() {

            }
        }
    }
}
