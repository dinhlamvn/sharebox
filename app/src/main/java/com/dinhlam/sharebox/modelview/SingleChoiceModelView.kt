package com.dinhlam.sharebox.modelview

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewSingleChoiceBinding

data class SingleChoiceModelView(
    val id: String,
    val text: String?,
    @DrawableRes val icon: Int,
    @ColorRes val textColor: Int = R.color.colorTextBlack
) : BaseListAdapter.BaseModelView("single_choice_$id") {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_single_choice

    class SingleChoiceViewHolder(
        private val binding: ModelViewSingleChoiceBinding, private val listener: ((Int) -> Unit)?
    ) : BaseListAdapter.BaseViewHolder<SingleChoiceModelView, ModelViewSingleChoiceBinding>(binding) {

        override fun onBind(model: SingleChoiceModelView, position: Int) {
            binding.root.setOnClickListener {
                listener?.invoke(position)
            }
            binding.textView.setTextColor(ContextCompat.getColor(buildContext, model.textColor))
            binding.textView.text = model.text
            binding.imageViewIcon.setImageResource(model.icon)
        }

        override fun onUnBind() {
        }
    }
}
