package com.dinhlam.sharebox.ui.home.modelview.recently

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareTextBinding

data class ShareTextRecentlyModelView(val id: String, val text: String?) :
    BaseListAdapter.BaseModelView("share_text_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextRecentlyModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextRecentlyModelView && other == this
    }

    class ShareTextViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareTextRecentlyModelView, ModelViewShareTextBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareTextBinding {
            return ModelViewShareTextBinding.bind(view)
        }

        override fun onBind(item: ShareTextRecentlyModelView, position: Int) {
            binding.textView.text = item.text
        }

        override fun onUnBind() {
        }
    }
}
