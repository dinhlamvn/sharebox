package com.dinhlam.sharebox.modelview

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup

data class LoadingModelView(val id: String) : BaseListAdapter.BaseModelView("loading_view_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_loading

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
