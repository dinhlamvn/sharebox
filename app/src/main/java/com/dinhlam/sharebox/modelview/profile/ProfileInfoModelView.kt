package com.dinhlam.sharebox.modelview.profile

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewProfileInfoBinding
import com.dinhlam.sharebox.extensions.asDisplayPoint
import com.dinhlam.sharebox.extensions.asProfileAge
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.utils.UserUtils

data class ProfileInfoModelView(
    val id: Int,
    val avatar: String,
    val name: String,
    val powerPoint: Int,
    val level: Int,
    val createdAt: Long,
) : BaseListAdapter.BaseModelView("user_info_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_profile_info

    class UserInfoViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<ProfileInfoModelView, ModelViewProfileInfoBinding>(
        view
    ) {

        override fun onCreateViewBinding(view: View): ModelViewProfileInfoBinding {
            return ModelViewProfileInfoBinding.bind(view)
        }

        override fun onBind(model: ProfileInfoModelView, position: Int) {
            ImageLoader.load(context, model.avatar, binding.imageAvatar, circle = true)
            binding.textViewName.text = model.name
            binding.pointPower.setPointText(model.powerPoint.asDisplayPoint())
            binding.pointLevel.setPointText(UserUtils.getLevelTitle(model.level))
            binding.pointLevel.setPointNameText(model.createdAt.asProfileAge())
        }

        override fun onUnBind() {
        }
    }
}
