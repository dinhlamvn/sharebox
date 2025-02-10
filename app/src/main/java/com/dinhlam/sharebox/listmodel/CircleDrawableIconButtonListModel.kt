package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelCircleDrawableIconButtonBinding
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.view.FontAwesomeIconView

data class CircleDrawableIconButtonListModel(
    val id: String,
    @DrawableRes val icon: Int,
    val iconStyle: FontAwesomeIconView.IconStyle = FontAwesomeIconView.IconStyle.SOLID,
    val margin: Spacing = Spacing.None,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<CircleDrawableIconButtonListModel, ListModelCircleDrawableIconButtonBinding>(
                ListModelCircleDrawableIconButtonBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: CircleDrawableIconButtonListModel, position: Int) {
                binding.root.setOnClickListener(model.onClick.prop)
                binding.root.updateMargin(model.margin)
                binding.icon.setImageResource(model.icon)
            }

            override fun onUnBind() {

            }
        }
    }
}