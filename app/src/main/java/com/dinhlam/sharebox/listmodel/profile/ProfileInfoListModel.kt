package com.dinhlam.sharebox.listmodel.profile

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelProfileInfoBinding
import com.dinhlam.sharebox.extensions.asDisplayPoint
import com.dinhlam.sharebox.extensions.asProfileAge
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.utils.UserUtils

data class ProfileInfoListModel(
    val id: String,
    val avatar: String,
    val name: String,
    val drama: Int,
    val level: Int,
    val joinDate: Long,
    val dramaIcon: String,
    val levelIcon: String,
    val actionSetting: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel("user_info_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return UserInfoViewHolderViewBinding(
            ListModelProfileInfoBinding.inflate(
                inflater,
                container,
                false
            )
        )
    }

    private class UserInfoViewHolderViewBinding(
        binding: ListModelProfileInfoBinding,
    ) : BaseListAdapter.BaseViewHolderViewBinding<ProfileInfoListModel, ListModelProfileInfoBinding>(
        binding
    ) {

        override fun onBind(model: ProfileInfoListModel, position: Int) {
            binding.iconSetting.setOnClickListener(model.actionSetting.prop)
            binding.imageAvatar.load(buildContext, model.avatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            binding.textViewName.text = model.name
            binding.pointDrama.setPointText(model.drama.asDisplayPoint())
            binding.pointLevel.setPointText(UserUtils.getLevelTitle(model.level))
            binding.pointLevel.setPointNameText(model.joinDate.asProfileAge())

            binding.pointDrama.setPointIcon(model.dramaIcon)
            binding.pointLevel.setPointIcon(model.levelIcon)
        }

        override fun onUnBind() {
        }
    }
}
