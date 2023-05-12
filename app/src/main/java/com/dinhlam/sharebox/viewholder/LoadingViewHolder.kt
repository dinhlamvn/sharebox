package com.dinhlam.sharebox.viewholder

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding
import com.dinhlam.sharebox.modelview.LoadingModelView

class LoadingViewHolder(binding: ModelViewLoadingBinding) :
    BaseListAdapter.BaseViewHolder<LoadingModelView, ModelViewLoadingBinding>(binding) {

    override fun onBind(model: LoadingModelView, position: Int) {
    }

    override fun onUnBind() {
    }
}
