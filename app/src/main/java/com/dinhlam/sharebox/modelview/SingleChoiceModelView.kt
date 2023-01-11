package com.dinhlam.sharebox.modelview

import android.view.View
import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewSingleChoiceBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat

data class SingleChoiceModelView(
    val id: String, val text: String?, @DrawableRes val icon: Int
) : BaseListAdapter.BaseModelView("single_choice_$id") {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_single_choice

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other.modelId == this.modelId
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other == this
    }

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
            binding.textView.text = item.text
            binding.textView.setDrawableCompat(start = item.icon)
        }

        override fun onUnBind() {
        }
    }
}
