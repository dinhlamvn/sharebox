package com.dinhlam.sharebox.base

import androidx.recyclerview.widget.GridLayoutManager

class BaseSpanSizeLookup(
    private val adapter: BaseListAdapter,
    private val spanCount: Int,
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
        val model = adapter.getModelAtPosition(position) ?: return spanCount
        val factorySpan = factory?.getSpanSize(model, position) ?: getSpanSizeInternal(model)
        return if (factorySpan <= 0) {
            getSpanSizeInternal(model)
        } else {
            factorySpan
        }
    }

    private fun getSpanSizeInternal(model: BaseListAdapter.BaseModelView): Int {
        val spanSizeConfig = model.getSpanSizeConfig()
        if (spanSizeConfig is SpanSizeConfig.Full) {
            return spanCount
        }
        return spanSizeConfig.spanCount
    }
}
