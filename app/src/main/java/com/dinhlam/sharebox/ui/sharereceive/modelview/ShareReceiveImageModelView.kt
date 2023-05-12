package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImageBinding
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareReceiveImageModelView(val id: String, val uri: Uri) :
    BaseListAdapter.BaseModelView("share_image_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_receive_image

    class ShareImageViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareReceiveImageModelView, ModelViewShareReceiveImageBinding>(
            view
        ) {
        override fun onCreateViewBinding(view: View): ModelViewShareReceiveImageBinding {
            return ModelViewShareReceiveImageBinding.bind(view)
        }

        override fun onBind(model: ShareReceiveImageModelView, position: Int) {
            ImageLoader.load(context, model.uri, binding.imageView)
        }

        override fun onUnBind() {
        }
    }
}
