package com.dinhlam.sharesaver.ui.share.modelview

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter

class ShareDefaultModelView : BaseListAdapter.BaseModelView("share_default") {
    override val layoutRes: Int
        get() = R.layout.share_item_default

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareDefaultModelView
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareDefaultModelView
    }
}
