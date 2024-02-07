package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveTextBinding
import com.dinhlam.sharebox.utils.Icons

data class ShareReceiveTextListModel(val id: String, val text: String?) :
    BaseListAdapter.BaseListModel("share_text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveTextListModel, ModelViewShareReceiveTextBinding>(
                ModelViewShareReceiveTextBinding.inflate(inflater, container, false)
            ) {

            init {
                binding.imageQuoteLeft.setImageDrawable(Icons.quoteLeftIcon(buildContext))
                binding.imageQuoteRight.setImageDrawable(Icons.quoteRightIcon(buildContext))
            }

            override fun onBind(model: ShareReceiveTextListModel, position: Int) {
                binding.textView.text = model.text
            }

            override fun onUnBind() {
            }
        }
    }
}
