package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveTextBinding

data class ShareReceiveTextModelView(val id: String, val text: String?) :
    BaseListAdapter.BaseModelView("share_text_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_receive_text

    class ShareTextViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareReceiveTextModelView, ModelViewShareReceiveTextBinding>(
            view
        ) {
        override fun onCreateViewBinding(view: View): ModelViewShareReceiveTextBinding {
            return ModelViewShareReceiveTextBinding.bind(view)
        }

        override fun onBind(model: ShareReceiveTextModelView, position: Int) {
            binding.textView.text = model.text
        }

        override fun onUnBind() {
        }
    }
}
