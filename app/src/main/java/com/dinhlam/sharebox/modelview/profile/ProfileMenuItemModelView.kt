package com.dinhlam.sharebox.modelview.profile

import android.view.LayoutInflater
import android.view.ViewGroup
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
    @ColorRes val textColor: Int = R.color.colorTextBlack,
    val listener: BaseListAdapter.NoHashProp<Function1<Int, Unit>> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseModelView("profile_menu_item_$id") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return ProfileMenuItemViewHolder(ModelViewProfileMenuItemBinding.inflate(inflater, container, false))
    }

    private class ProfileMenuItemViewHolder(
        binding: ModelViewProfileMenuItemBinding
    ) : BaseListAdapter.BaseViewHolder<ProfileMenuItemModelView, ModelViewProfileMenuItemBinding>(
        binding
    ) {

        override fun onBind(model: ProfileMenuItemModelView, position: Int) {
            binding.root.setOnClickListener {
                model.listener.prop?.invoke(position)
            }
            binding.textView.setTextColor(ContextCompat.getColor(buildContext, model.textColor))
            binding.textView.text = model.text
            binding.imageViewIcon.setImageResource(model.icon)
        }

        override fun onUnBind() {
        }
    }
}
