package com.dinhlam.sharesaver.ui.share.modelview

import android.net.Uri
import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemImageBinding
import com.dinhlam.sharesaver.loader.ImageLoader

data class ShareImageModelView(val id: String, val uri: Uri) :
    BaseListAdapter.BaseModelView("share_image_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.share_item_image

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareImageModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareImageModelView && other == this
    }

    class ShareImageViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareImageModelView, ShareItemImageBinding>(view) {
        override fun onCreateViewBinding(view: View): ShareItemImageBinding {
            return ShareItemImageBinding.bind(view)
        }

        override fun onBind(item: ShareImageModelView, position: Int) {
            ImageLoader.load(context, item.uri, binding.imageView)
        }

        override fun onUnBind() {
        }
    }
}
