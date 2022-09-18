package com.dinhlam.sharesaver.ui.home.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ImageItemViewBinding
import com.dinhlam.sharesaver.loader.ImageLoader
import com.dinhlam.sharesaver.ui.home.modelview.HomeItemModelView

class HomeImageViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeImageModelView, ImageItemViewBinding>(view) {

    override fun onBind(item: HomeItemModelView.HomeImageModelView, position: Int) {
        ImageLoader.load(context, item.uri, binding.imageView)
    }

    override fun onUnBind() {

    }

    override fun onCreateViewBinding(view: View): ImageItemViewBinding {
        return ImageItemViewBinding.bind(view)
    }
}