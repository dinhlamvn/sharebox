package com.dinhlam.sharesaver.ui.share.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemMultipleImageBinding
import com.dinhlam.sharesaver.loader.ImageLoader
import com.dinhlam.sharesaver.ui.share.modelview.ShareMultipleImageModelView

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