package com.dinhlam.sharesaver.ui.share.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemTextBinding
import com.dinhlam.sharesaver.ui.share.modelview.ShareTextModelView

class ShareTextViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<ShareTextModelView, ShareItemTextBinding>(view) {
    override fun onCreateViewBinding(view: View): ShareItemTextBinding {
        return ShareItemTextBinding.bind(view)
    }

    override fun onBind(item: ShareTextModelView, position: Int) {
        binding.textView.text = item.text
    }

    override fun onUnBind() {
    }
}