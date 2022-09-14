package com.dinhlam.sharekeeper.viewholder

import android.view.View
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.ModelViewLoadingBinding
import com.dinhlam.sharekeeper.modelview.LoadingModelView

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