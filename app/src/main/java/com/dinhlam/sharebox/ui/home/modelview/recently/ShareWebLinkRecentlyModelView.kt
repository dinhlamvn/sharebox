package com.dinhlam.sharebox.ui.home.modelview.recently

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareWebLinkBinding

data class ShareWebLinkRecentlyModelView(val id: String, val url: String?) :
    BaseListAdapter.BaseModelView("share_url_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_web_link

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareWebLinkRecentlyModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareWebLinkRecentlyModelView && other == this
    }

    class ShareWebLinkViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareWebLinkRecentlyModelView, ModelViewShareWebLinkBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareWebLinkBinding {
            return ModelViewShareWebLinkBinding.bind(view)
        }

        override fun onBind(item: ShareWebLinkRecentlyModelView, position: Int) {
            binding.textViewUrl.text = item.url
        }

        override fun onUnBind() {
        }
    }
}
