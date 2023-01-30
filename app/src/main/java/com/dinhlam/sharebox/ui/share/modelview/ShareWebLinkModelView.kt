package com.dinhlam.sharebox.ui.share.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareWebLinkBinding

data class ShareWebLinkModelView(val id: String, val url: String?) :
    BaseListAdapter.BaseModelView("share_url_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_web_link

    class ShareWebLinkViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareWebLinkModelView, ModelViewShareWebLinkBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareWebLinkBinding {
            return ModelViewShareWebLinkBinding.bind(view)
        }

        override fun onBind(item: ShareWebLinkModelView, position: Int) {
            binding.linkPreview.setLink(item.url)
        }

        override fun onUnBind() {
        }
    }
}
