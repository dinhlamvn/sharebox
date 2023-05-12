package com.dinhlam.sharebox.modelview

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
    override val modelLayoutRes: Int
        get() = R.layout.model_view_divider


    class DividerViewHolder(val binding: ModelViewDividerBinding) :
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
