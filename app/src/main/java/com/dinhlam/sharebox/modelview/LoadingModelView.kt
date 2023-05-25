package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding

data class LoadingModelView(val id: String, val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) :
    BaseListAdapter.BaseModelView("loading_view_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<LoadingModelView, ModelViewLoadingBinding>(
            ModelViewLoadingBinding.inflate(inflater, container, false)
        ) {
            override fun onBind(model: LoadingModelView, position: Int) {
                binding.root.updateLayoutParams {
                    height = model.height
                }
            }

            override fun onUnBind() {

            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
