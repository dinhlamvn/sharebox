package com.dinhlam.sharekeeper.ui.share.viewholder

import android.view.View
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.ShareItemMultipleImageBinding
import com.dinhlam.sharekeeper.loader.ImageLoader
import com.dinhlam.sharekeeper.ui.share.modelview.ShareMultipleImageModelView

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