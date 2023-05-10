package com.dinhlam.sharebox.modelview.profile

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewProfileUserInfoBinding
import com.dinhlam.sharebox.extensions.asDisplayPoint
import com.dinhlam.sharebox.loader.ImageLoader

data class ProfileUserInfoModelView(
    val id: Int,
    val avatar: String,
    val name: String,
    val powerPoint: Int
) : BaseListAdapter.BaseModelView("user_info_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_profile_user_info

    class UserInfoViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<ProfileUserInfoModelView, ModelViewProfileUserInfoBinding>(
        view
    ) {

        override fun onCreateViewBinding(view: View): ModelViewProfileUserInfoBinding {
            return ModelViewProfileUserInfoBinding.bind(view)
        }

        override fun onBind(model: ProfileUserInfoModelView, position: Int) {
            ImageLoader.load(context, model.avatar, binding.imageAvatar, circle = true)
            binding.textViewName.text = model.name
            binding.pointPower.setPointText(model.powerPoint.asDisplayPoint())
            binding.pointLevel.setPointText("Junior Member")
            binding.pointLevel.setPointNameText("2 yrs")
        }

        override fun onUnBind() {
        }
    }
}
