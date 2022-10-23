package com.dinhlam.sharesaver.ui.share.modelview

import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemDefaultBinding

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
