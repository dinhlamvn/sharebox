package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveUrlBinding

data class ShareReceiveUrlListModel(val id: String, val url: String?) :
    BaseListAdapter.BaseListModel("share_url_$id") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveUrlListModel, ModelViewShareReceiveUrlBinding>(
                ModelViewShareReceiveUrlBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ShareReceiveUrlListModel, position: Int) {
                binding.linkPreview.setLink(model.url)
            }

            override fun onUnBind() {
            }
        }
    }
}
