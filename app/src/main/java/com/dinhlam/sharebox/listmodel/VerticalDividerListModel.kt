package com.dinhlam.sharebox.listmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.extensions.updateWidth
import com.dinhlam.sharebox.model.Spacing
import com.google.android.material.divider.MaterialDivider

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
        return DividerViewHolderViewHolder(container.context)
    }

    private class DividerViewHolderViewHolder(context: Context) :
        BaseListAdapter.BaseViewHolderCustomView<VerticalDividerListModel, MaterialDivider>(
            MaterialDivider(context)
        ) {

        init {
            view.dividerThickness = 1.dp()
        }

        private val defaultColor = view.dividerColor

        override fun onBind(model: VerticalDividerListModel, position: Int) {
            view.updateWidth(model.width)
            view.updateMargin(model.margin)

            view.dividerThickness = model.height

            model.dividerColor.takeIf { color -> color != 0 }?.let { takenColor ->
                view.dividerColor = ContextCompat.getColor(
                    buildContext, takenColor
                )
            } ?: apply {
                view.dividerColor = defaultColor
            }
        }

        override fun onUnBind() {
        }
    }
}
