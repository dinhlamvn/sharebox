package com.dinhlam.keepmyshare.ui.home.modelview

import com.dinhlam.keepmyshare.R
import com.dinhlam.keepmyshare.base.BaseListAdapter

sealed class HomeItemModelView {

    data class HomeTextModelView(
        val id: String,
        val text: String = ""
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.text_item_view

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other == this
        }
    }


    data class HomeImageModelView(
        val id: String,
        val url: String = ""
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
