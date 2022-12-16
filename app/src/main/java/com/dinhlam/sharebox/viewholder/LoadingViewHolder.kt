package com.dinhlam.sharebox.viewholder

import android.view.View
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding
import com.dinhlam.sharebox.modelview.LoadingModelView

class LoadingViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<LoadingModelView, ModelViewLoadingBinding>(view) {
    override fun onCreateViewBinding(view: View): ModelViewLoadingBinding {
        return ModelViewLoadingBinding.bind(view)
    }

    override fun onBind(item: LoadingModelView, position: Int) {
    }

    override fun onUnBind() {
    }
}
