package com.dinhlam.sharesaver.ui.share.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemImageBinding
import com.dinhlam.sharesaver.loader.ImageLoader
import com.dinhlam.sharesaver.ui.share.modelview.ShareImageModelView

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