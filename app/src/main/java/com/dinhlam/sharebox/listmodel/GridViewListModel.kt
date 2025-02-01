package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup

data class GridViewListModel(
    val id: String,
    val listModels: List<BaseListAdapter.BaseListModel>,
    val spanCount: Int = 2
) : BaseListAdapter.BaseListModel(id) {

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderCustomView<GridViewListModel, RecyclerView>(
                RecyclerView(
                    container.context
                ).apply {
                    layoutManager = GridLayoutManager(context, spanCount)
                }) {

            private val carouselAdapter = BaseListAdapter.create()

            init {
                view.adapter = carouselAdapter
                carouselAdapter.requestBuildListModels()
            }

            override fun onBind(model: GridViewListModel, position: Int) {
                carouselAdapter.requestBuildListModels(model.listModels)
            }

            override fun onUnBind() {
            }
        }
    }
}