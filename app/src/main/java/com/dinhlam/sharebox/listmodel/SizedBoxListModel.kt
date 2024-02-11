package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewSizedBoxBinding

data class SizedBoxListModel(
    val id: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    @ColorRes val backgroundColor: Int = 0
) : BaseListAdapter.BaseListModel(id) {

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return DividerViewHolder(ModelViewSizedBoxBinding.inflate(inflater, container, false))
    }

    private class DividerViewHolder(binding: ModelViewSizedBoxBinding) :
        BaseListAdapter.BaseViewHolder<SizedBoxListModel, ModelViewSizedBoxBinding>(binding) {

        override fun onBind(model: SizedBoxListModel, position: Int) {
            binding.root.updateLayoutParams {
                width = model.width
                height = model.height
            }

            model.backgroundColor.takeIf { color -> color != 0 }?.let { takenColor ->
                binding.root.dividerColor = ContextCompat.getColor(
                    buildContext, takenColor
                )
            }
        }

        override fun onUnBind() {
        }
    }
}
