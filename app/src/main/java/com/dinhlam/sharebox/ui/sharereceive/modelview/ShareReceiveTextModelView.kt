package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.LayoutInflater
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveTextBinding

data class ShareReceiveTextModelView(val id: String, val text: String?) :
    BaseListAdapter.BaseModelView("share_text_$id") {

    override fun createViewHolder(inflater: LayoutInflater): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveTextModelView, ModelViewShareReceiveTextBinding>(
                ModelViewShareReceiveTextBinding.inflate(inflater)
            ) {

            override fun onBind(model: ShareReceiveTextModelView, position: Int) {
                binding.textView.text = model.text
            }

            override fun onUnBind() {
            }
        }
    }
}
