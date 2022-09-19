package com.dinhlam.sharesaver.modelview

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter

object LoadingModelView : BaseListAdapter.BaseModelView("loading_view") {
    override val layoutRes: Int
        get() = R.layout.model_view_loading

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is LoadingModelView
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is LoadingModelView
    }
}