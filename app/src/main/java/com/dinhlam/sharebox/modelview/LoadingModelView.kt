package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding

data class LoadingModelView(val id: String) : BaseListAdapter.BaseModelView("loading_view_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<LoadingModelView, ModelViewLoadingBinding>(
            ModelViewLoadingBinding.inflate(inflater)
        ) {
            override fun onBind(model: LoadingModelView, position: Int) {

            }

            override fun onUnBind() {

            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
