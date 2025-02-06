package com.dinhlam.sharebox.listmodel

import android.net.Uri
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelDownloadItemBinding
import com.dinhlam.sharebox.extensions.getVideoThumbnail
import com.dinhlam.sharebox.imageloader.load

data class DownloadItemListModel(
    val id: String,
    val thumbnail: String?,
    val title: String,
    val isVideo: Boolean,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener>
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
            if (model.isVideo) {
                val bitmap = buildContext.getVideoThumbnail(Uri.parse(model.thumbnail))
                binding.thumbnail.setImageBitmap(bitmap)
            } else {
                binding.thumbnail.load(buildContext, model.thumbnail)
            }
            binding.title.text = model.title
            binding.root.setOnClickListener(model.actionClick.prop)
        }

        override fun onUnBind() {

        }
    }
}
