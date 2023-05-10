package com.dinhlam.sharebox.modelview

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import kotlin.random.Random

object LoadingModelView : BaseListAdapter.BaseModelView("loading_view_${Random.nextInt()}") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_loading

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
