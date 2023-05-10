package com.dinhlam.sharebox.modelview.profile

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewProfileMenuItemBinding

data class ProfileMenuItemModelView(
    val id: String,
    val text: String?,
    @DrawableRes val icon: Int,
    @ColorRes val textColor: Int = R.color.colorTextBlack
) : BaseListAdapter.BaseModelView("profile_menu_item_$id") {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_profile_menu_item

    class ProfileMenuItemViewHolder(
        view: View, private val listener: ((Int) -> Unit)?
    ) : BaseListAdapter.BaseViewHolder<ProfileMenuItemModelView, ModelViewProfileMenuItemBinding>(
        view
    ) {

        override fun onCreateViewBinding(view: View): ModelViewProfileMenuItemBinding {
            return ModelViewProfileMenuItemBinding.bind(view)
        }

        override fun onBind(model: ProfileMenuItemModelView, position: Int) {
            binding.root.setOnClickListener {
                listener?.invoke(position)
            }
            binding.textView.setTextColor(ContextCompat.getColor(context, model.textColor))
            binding.textView.text = model.text
            binding.imageViewIcon.setImageResource(model.icon)
        }

        override fun onUnBind() {
        }
    }
}
