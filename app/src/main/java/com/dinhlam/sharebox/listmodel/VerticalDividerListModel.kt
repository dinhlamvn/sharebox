package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ListModelVerticalDividerBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.extensions.updateWidth
import com.dinhlam.sharebox.model.Spacing

data class VerticalDividerListModel(
    val id: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = 1.dp(),
    @ColorRes val dividerColor: Int = 0,
    val margin: Spacing = Spacing.None,
) : BaseListAdapter.BaseListModel(id) {

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return DividerViewHolderViewBinding(
            ListModelVerticalDividerBinding.inflate(
                inflater,
                container,
                false
            )
        )
    }

    private class DividerViewHolderViewBinding(binding: ListModelVerticalDividerBinding) :
        BaseListAdapter.BaseViewHolderViewBinding<VerticalDividerListModel, ListModelVerticalDividerBinding>(
            binding
        ) {

        private val defaultColor = binding.root.dividerColor


        override fun onBind(model: VerticalDividerListModel, position: Int) {
            binding.root.updateWidth(model.width)
            binding.root.updateMargin(model.margin)

            binding.divider.dividerThickness = model.height

            model.dividerColor.takeIf { color -> color != 0 }?.let { takenColor ->
                binding.root.dividerColor = ContextCompat.getColor(
                    buildContext, takenColor
                )
            } ?: apply {
                binding.root.dividerColor = defaultColor
            }
        }

        override fun onUnBind() {
        }
    }
}
