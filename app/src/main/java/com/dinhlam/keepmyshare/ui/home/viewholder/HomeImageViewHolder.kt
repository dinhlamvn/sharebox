package com.dinhlam.keepmyshare.ui.home.viewholder

import android.view.View
import com.dinhlam.keepmyshare.base.BaseListAdapter
import com.dinhlam.keepmyshare.databinding.ImageItemViewBinding
import com.dinhlam.keepmyshare.loader.ImageLoader
import com.dinhlam.keepmyshare.ui.home.modelview.HomeItemModelView

class HomeImageViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeImageModelView, ImageItemViewBinding>(view) {

    override fun onBind(item: HomeItemModelView.HomeImageModelView, position: Int) {
        ImageLoader.load(context, item.url, binding.imageView)
    }

    override fun onUnBind() {

    }

    override fun onCreateViewBinding(view: View): ImageItemViewBinding {
        return ImageItemViewBinding.bind(view)
    }
}