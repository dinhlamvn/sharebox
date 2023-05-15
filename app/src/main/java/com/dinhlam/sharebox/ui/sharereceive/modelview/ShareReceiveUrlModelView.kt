package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.LayoutInflater
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveUrlBinding

data class ShareReceiveUrlModelView(val id: String, val url: String?) :
    BaseListAdapter.BaseModelView("share_url_$id") {

    override fun createViewHolder(inflater: LayoutInflater): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveUrlModelView, ModelViewShareReceiveUrlBinding>(
                ModelViewShareReceiveUrlBinding.inflate(inflater)
            ) {

            override fun onBind(model: ShareReceiveUrlModelView, position: Int) {
                binding.linkPreview.setLink(model.url)
            }

            override fun onUnBind() {
            }
        }
    }
}
