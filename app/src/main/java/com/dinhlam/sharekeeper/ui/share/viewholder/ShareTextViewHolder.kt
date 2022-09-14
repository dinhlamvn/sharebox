package com.dinhlam.sharekeeper.ui.share.viewholder

import android.view.View
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.ShareItemTextBinding
import com.dinhlam.sharekeeper.ui.share.modelview.ShareTextModelView

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