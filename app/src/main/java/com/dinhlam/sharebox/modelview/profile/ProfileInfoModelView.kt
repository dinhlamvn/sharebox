package com.dinhlam.sharebox.modelview.profile

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewProfileInfoBinding
import com.dinhlam.sharebox.extensions.asDisplayPoint
import com.dinhlam.sharebox.extensions.asProfileAge
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.UserUtils

data class ProfileInfoModelView(
    val id: String,
    val avatar: String,
    val name: String,
    val drama: Int,
    val level: Int,
    val joinDate: Long,
    val actionSetting: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseModelView("user_info_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return UserInfoViewHolder(ModelViewProfileInfoBinding.inflate(inflater, container, false))
    }

    private class UserInfoViewHolder(
        binding: ModelViewProfileInfoBinding,
    ) : BaseListAdapter.BaseViewHolder<ProfileInfoModelView, ModelViewProfileInfoBinding>(
        binding
    ) {

        init {
            binding.imageSetting.setImageDrawable(Icons.settingIcon(buildContext))
        }

        override fun onBind(model: ProfileInfoModelView, position: Int) {
            binding.imageSetting.setOnClickListener(model.actionSetting.prop)
            ImageLoader.INSTANCE.load(buildContext, model.avatar, binding.imageAvatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            binding.textViewName.text = model.name
            binding.pointDrama.setPointText(model.drama.asDisplayPoint())
            binding.pointLevel.setPointText(UserUtils.getLevelTitle(model.level))
            binding.pointLevel.setPointNameText(model.joinDate.asProfileAge())
        }

        override fun onUnBind() {
        }
    }
}
