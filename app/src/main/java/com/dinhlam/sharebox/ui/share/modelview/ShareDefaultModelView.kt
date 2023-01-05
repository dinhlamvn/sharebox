package com.dinhlam.sharebox.ui.share.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ShareItemDefaultBinding

class ShareDefaultModelView : BaseListAdapter.BaseModelView("share_default") {
    override val modelLayoutRes: Int
        get() = R.layout.share_item_default

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareDefaultModelView
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareDefaultModelView
    }

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
}
