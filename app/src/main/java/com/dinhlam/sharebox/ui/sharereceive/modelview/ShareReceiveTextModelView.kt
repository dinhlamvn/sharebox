package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveTextBinding
import com.dinhlam.sharebox.utils.Icons

data class ShareReceiveTextModelView(val id: String, val text: String?) :
    BaseListAdapter.BaseModelView("share_text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveTextModelView, ModelViewShareReceiveTextBinding>(
                ModelViewShareReceiveTextBinding.inflate(inflater, container, false)
            ) {

            init {
                binding.imageQuoteLeft.setImageDrawable(Icons.quoteLeftIcon(buildContext))
                binding.imageQuoteRight.setImageDrawable(Icons.quoteRightIcon(buildContext))
            }

            override fun onBind(model: ShareReceiveTextModelView, position: Int) {
                binding.textView.text = model.text
            }

            override fun onUnBind() {
            }
        }
    }
}
