package com.dinhlam.sharebox.ui.sharereceive.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelShareFileBinding
import com.dinhlam.sharebox.extensions.asHumanReadableSize

data class ShareReceiveFileListModel(
    val id: String,
    val fileName: String,
    val fileSize: Double
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return ShareReceiveFileViewHolder(
            ListModelShareFileBinding.inflate(
                inflater,
                container,
                false
            )
        )
    }

    private class ShareReceiveFileViewHolder(binding: ListModelShareFileBinding) :
        BaseListAdapter.BaseViewHolderViewBinding<ShareReceiveFileListModel, ListModelShareFileBinding>(
            binding
        ) {
        override fun onBind(model: ShareReceiveFileListModel, position: Int) {
            binding.textFileName.text = model.fileName
            binding.textFileSize.text = model.fileSize.asHumanReadableSize()
        }

        override fun onUnBind() {

        }
    }
}
