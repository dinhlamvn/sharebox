package com.dinhlam.sharekeeper.ui.share.modelview

import android.net.Uri
import com.dinhlam.sharekeeper.R
import com.dinhlam.sharekeeper.base.BaseListAdapter

data class ShareImageModelView(val id: String, val uri: Uri) : BaseListAdapter.BaseModelView("share_image_$id") {
    override val layoutRes: Int
        get() = R.layout.share_item_image

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareImageModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareImageModelView && other == this
    }
}
