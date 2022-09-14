package com.dinhlam.sharekeeper.ui.share.viewholder

import android.view.View
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.ShareItemImageBinding
import com.dinhlam.sharekeeper.loader.ImageLoader
import com.dinhlam.sharekeeper.ui.share.modelview.ShareImageModelView

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