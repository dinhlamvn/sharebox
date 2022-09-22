package com.dinhlam.sharesaver.base

import androidx.recyclerview.widget.GridLayoutManager

class BaseSpanSizeLookup(
    private val adapter: BaseListAdapter<BaseListAdapter.BaseModelView>,
    private val gridLayoutManager: GridLayoutManager,
    private val factory: SpanSizeFactory? = null
) : GridLayoutManager.SpanSizeLookup() {

    fun interface SpanSizeFactory {
        fun getSpanSize(model: BaseListAdapter.BaseModelView, position: Int): Int
    }

    sealed class SpanSizeConfig(open val spanCount: Int = 1) {
        object Normal : SpanSizeConfig(1)
        object Full : SpanSizeConfig(-1)
        data class Custom(override val spanCount: Int) : SpanSizeConfig(spanCount)
    }

    override fun getSpanSize(position: Int): Int {
        val model = adapter.getModelAtPosition(position) ?: return 1
        return factory?.getSpanSize(model, position) ?: getSpanSizeInternal(model)
    }

    private fun getSpanSizeInternal(model: BaseListAdapter.BaseModelView): Int {
        val spanSizeConfig = model.getSpanSizeConfig()
        if (spanSizeConfig is SpanSizeConfig.Full) {
            return gridLayoutManager.spanCount
        }
        return spanSizeConfig.spanCount
    }
}