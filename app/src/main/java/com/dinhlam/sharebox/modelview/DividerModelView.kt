package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewDividerBinding
import com.dinhlam.sharebox.extensions.dp

data class DividerModelView(
    val id: String, val size: Int = 1, @ColorRes val color: Int = R.color.colorDividerLight
) : BaseListAdapter.BaseModelView(id) {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return DividerViewHolder(ModelViewDividerBinding.inflate(inflater, container, false))
    }

    private class DividerViewHolder(binding: ModelViewDividerBinding) :
        BaseListAdapter.BaseViewHolder<DividerModelView, ModelViewDividerBinding>(binding) {

        override fun onBind(model: DividerModelView, position: Int) {
            binding.root.updateLayoutParams {
                height = model.size.dp(buildContext)
            }
            binding.root.setBackgroundColor(ContextCompat.getColor(buildContext, model.color))
        }

        override fun onUnBind() {
        }
    }
}
