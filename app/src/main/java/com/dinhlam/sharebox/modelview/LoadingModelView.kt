package com.dinhlam.sharebox.modelview

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding

data class LoadingModelView(val id: String) : BaseListAdapter.BaseModelView("loading_view_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_loading

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class LoadingViewHolder(binding: ModelViewLoadingBinding) :
        BaseListAdapter.BaseViewHolder<LoadingModelView, ModelViewLoadingBinding>(binding) {

        override fun onBind(model: LoadingModelView, position: Int) {
        }

        override fun onUnBind() {
        }
    }
}
