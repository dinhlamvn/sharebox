package com.dinhlam.sharekeeper.ui.share.modelview

import com.dinhlam.sharekeeper.R
import com.dinhlam.sharekeeper.base.BaseListAdapter

data class ShareTextModelView(val id: String, val text: String = "") : BaseListAdapter.BaseModelView("share_text_$id") {
    override val layoutRes: Int
        get() = R.layout.share_item_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextModelView && other == this
    }
}
