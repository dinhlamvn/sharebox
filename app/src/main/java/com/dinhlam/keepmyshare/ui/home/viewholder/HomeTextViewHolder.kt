package com.dinhlam.keepmyshare.ui.home.viewholder

import android.view.View
import com.dinhlam.keepmyshare.base.BaseListAdapter
import com.dinhlam.keepmyshare.databinding.TextItemViewBinding
import com.dinhlam.keepmyshare.ui.home.modelview.HomeItemModelView

class HomeTextViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeTextModelView, TextItemViewBinding>(view) {

    override fun onBind(item: HomeItemModelView.HomeTextModelView, position: Int) {
        binding.textView.text = item.text
    }

    override fun onUnBind() {

    }

    override fun onCreateViewBinding(view: View): TextItemViewBinding {
        return TextItemViewBinding.bind(view)
    }
}