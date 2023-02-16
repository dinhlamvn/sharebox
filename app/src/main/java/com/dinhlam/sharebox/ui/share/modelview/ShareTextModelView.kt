package com.dinhlam.sharebox.ui.share.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareTextBinding

data class ShareTextModelView(val id: String, val text: String?) :
    BaseListAdapter.BaseModelView("share_text_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_text

    class ShareTextViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareTextModelView, ModelViewShareTextBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareTextBinding {
            return ModelViewShareTextBinding.bind(view)
        }

        override fun onBind(model: ShareTextModelView, position: Int) {
            binding.textView.text = model.text
        }

        override fun onUnBind() {
        }
    }
}
