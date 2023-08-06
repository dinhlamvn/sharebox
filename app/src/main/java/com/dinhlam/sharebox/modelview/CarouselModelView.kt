package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewCarouselBinding

data class CarouselModelView(val id: String, val modelViews: List<BaseListAdapter.BaseModelView>) :
    BaseListAdapter.BaseModelView(id) {

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<CarouselModelView, ModelViewCarouselBinding>(
            ModelViewCarouselBinding.inflate(inflater, container, false)
        ) {

            private val carouselAdapter = BaseListAdapter.createAdapter {
                models.forEach { it.attachTo(this) }
            }

            private val models = mutableListOf<BaseListAdapter.BaseModelView>()

            init {
                binding.recyclerView.adapter = carouselAdapter
                carouselAdapter.requestBuildModelViews()
            }

            override fun onBind(model: CarouselModelView, position: Int) {
                models.clear()
                models.addAll(model.modelViews)
                carouselAdapter.requestBuildModelViews()
            }

            override fun onUnBind() {
                models.clear()
                carouselAdapter.requestBuildModelViews()
            }
        }
    }
}