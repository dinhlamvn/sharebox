package com.dinhlam.sharebox.modelview

import android.view.View
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
        view: View, private val listener: ((Int) -> Unit)?
    ) : BaseListAdapter.BaseViewHolder<SingleChoiceModelView, ModelViewSingleChoiceBinding>(view) {

        override fun onCreateViewBinding(view: View): ModelViewSingleChoiceBinding {
            return ModelViewSingleChoiceBinding.bind(view)
        }

        override fun onBind(item: SingleChoiceModelView, position: Int) {
            binding.root.setOnClickListener {
                listener?.invoke(position)
            }
            binding.textView.setTextColor(ContextCompat.getColor(context, item.textColor))
            binding.textView.text = item.text
            binding.imageViewIcon.setImageResource(item.icon)
        }

        override fun onUnBind() {
        }
    }
}
