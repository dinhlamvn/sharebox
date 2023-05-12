package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveUrlBinding

data class ShareReceiveUrlModelView(val id: String, val url: String?) :
    BaseListAdapter.BaseModelView("share_url_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_receive_url

    class ShareWebLinkViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareReceiveUrlModelView, ModelViewShareReceiveUrlBinding>(
            view
        ) {
        override fun onCreateViewBinding(view: View): ModelViewShareReceiveUrlBinding {
            return ModelViewShareReceiveUrlBinding.bind(view)
        }

        override fun onBind(model: ShareReceiveUrlModelView, position: Int) {
            binding.linkPreview.setLink(model.url)
        }

        override fun onUnBind() {
        }
    }
}
