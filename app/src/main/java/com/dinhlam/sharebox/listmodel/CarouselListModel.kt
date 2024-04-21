package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewCarouselBinding
import com.dinhlam.sharebox.extensions.updateHeight

data class CarouselListModel(
    val id: String,
    val listModels: List<BaseListAdapter.BaseListModel>,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) :
    BaseListAdapter.BaseListModel(id) {

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<CarouselListModel, ModelViewCarouselBinding>(
            ModelViewCarouselBinding.inflate(inflater, container, false)
        ) {

            private val carouselAdapter = BaseListAdapter.createAdapter {
                models.forEach { it.attachTo(this) }
            }

            private val models = mutableListOf<BaseListAdapter.BaseListModel>()

            init {
                binding.recyclerView.adapter = carouselAdapter
                carouselAdapter.requestBuildListModels()
            }

            override fun onBind(model: CarouselListModel, position: Int) {
                binding.root.updateHeight(model.height)
                models.clear()
                models.addAll(model.listModels)
                carouselAdapter.requestBuildListModels()
            }

            override fun onUnBind() {
            }
        }
    }
}