package com.dinhlam.sharesaver.ui.share.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemDefaultBinding

class ShareDefaultViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<BaseListAdapter.BaseModelView, ShareItemDefaultBinding>(view) {
    override fun onCreateViewBinding(view: View): ShareItemDefaultBinding {
        return ShareItemDefaultBinding.bind(view)
    }

    override fun onBind(item: BaseListAdapter.BaseModelView, position: Int) {
    }

    override fun onUnBind() {
    }
}