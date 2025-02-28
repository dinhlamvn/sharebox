package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelDownloadItemBinding
import com.dinhlam.sharebox.imageloader.load

data class DownloadItemListModel(
    val id: String,
    val thumbnail: String?,
    val title: String,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onThumbnailClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    )
) : BaseListAdapter.BaseListModel(id) {
    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return DownloadItemViewHolder(inflater, container)
    }

    private class DownloadItemViewHolder(layoutInflater: LayoutInflater, container: ViewGroup) :
        BaseListAdapter.BaseViewHolderViewBinding<DownloadItemListModel, ListModelDownloadItemBinding>(
            ListModelDownloadItemBinding.inflate(layoutInflater, container, false)
        ) {
        override fun onBind(model: DownloadItemListModel, position: Int) {
            binding.thumbnail.load(buildContext, model.thumbnail)
            binding.title.text = model.title
            binding.root.setOnClickListener(model.actionClick.prop)
            binding.thumbnail.setOnClickListener(model.onThumbnailClick.prop)
        }

        override fun onUnBind() {

        }
    }
}
