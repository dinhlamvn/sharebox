package com.dinhlam.sharebox.ui.share.modelview

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ShareItemMultipleImageBinding
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareMultipleImageModelView(val id: String, val uri: Uri) :
    BaseListAdapter.BaseModelView("share_multiple_image_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.share_item_multiple_image

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareMultipleImageModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareMultipleImageModelView && other == this
    }

    class ShareMultipleImageViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareMultipleImageModelView, ShareItemMultipleImageBinding>(view) {
        override fun onCreateViewBinding(view: View): ShareItemMultipleImageBinding {
            return ShareItemMultipleImageBinding.bind(view)
        }

        override fun onBind(item: ShareMultipleImageModelView, position: Int) {
            ImageLoader.load(context, item.uri, binding.imageView)
        }

        override fun onUnBind() {
        }
    }
}
