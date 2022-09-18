package com.dinhlam.sharesaver.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewLoadingBinding
import com.dinhlam.sharesaver.modelview.LoadingModelView

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