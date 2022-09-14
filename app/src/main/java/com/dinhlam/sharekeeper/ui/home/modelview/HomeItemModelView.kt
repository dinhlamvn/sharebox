package com.dinhlam.sharekeeper.ui.home.modelview

import android.net.Uri
import com.dinhlam.sharekeeper.R
import com.dinhlam.sharekeeper.base.BaseListAdapter

sealed class HomeItemModelView {

    data class HomeTextModelView(
        val id: String,
        val text: String = "",
        val createdAt: Long
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.model_view_home_share_text

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other == this
        }
    }


    data class HomeImageModelView(
        val id: String,
        val uri: Uri
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.image_item_view

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other == this
        }
    }
}
