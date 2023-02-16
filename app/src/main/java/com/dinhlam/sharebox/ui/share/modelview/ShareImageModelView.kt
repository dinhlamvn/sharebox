package com.dinhlam.sharebox.ui.share.modelview

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareImageBinding
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareImageModelView(val id: String, val uri: Uri) :
    BaseListAdapter.BaseModelView("share_image_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_image

    class ShareImageViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareImageModelView, ModelViewShareImageBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareImageBinding {
            return ModelViewShareImageBinding.bind(view)
        }

        override fun onBind(model: ShareImageModelView, position: Int) {
            ImageLoader.load(context, model.uri, binding.imageView)
        }

        override fun onUnBind() {
        }
    }
}
