package com.dinhlam.sharesaver.ui.share.modelview

import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewShareWebLinkBinding

data class ShareWebLinkModelView(val id: String, val url: String?) :
    BaseListAdapter.BaseModelView("share_url_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_web_link

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareWebLinkModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareWebLinkModelView && other == this
    }

    class ShareWebLinkViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareWebLinkModelView, ModelViewShareWebLinkBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareWebLinkBinding {
            return ModelViewShareWebLinkBinding.bind(view)
        }

        override fun onBind(item: ShareWebLinkModelView, position: Int) {
            binding.textViewUrl.text = item.url
        }

        override fun onUnBind() {
        }
    }
}
