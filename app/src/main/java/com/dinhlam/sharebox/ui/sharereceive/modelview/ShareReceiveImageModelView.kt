package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImageBinding
import com.dinhlam.sharebox.imageloader.ImageLoader

data class ShareReceiveImageModelView(val id: String, val uri: Uri) :
    BaseListAdapter.BaseModelView("share_image_$id") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveImageModelView, ModelViewShareReceiveImageBinding>(
                ModelViewShareReceiveImageBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ShareReceiveImageModelView, position: Int) {
                ImageLoader.instance.load(buildContext, model.uri, binding.imageView)
            }

            override fun onUnBind() {
            }
        }
    }
}
