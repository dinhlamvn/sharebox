package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding

data class LoadingListModel(
    val id: String,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val message: String? = null
) :
    BaseListAdapter.BaseListModel("loading_view_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<LoadingListModel, ModelViewLoadingBinding>(
                ModelViewLoadingBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: LoadingListModel, position: Int) {
                binding.root.updateLayoutParams {
                    height = model.height
                }
                binding.text.text = model.message
                binding.text.isVisible = !model.message.isNullOrBlank()
            }

            override fun onUnBind() {

            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
