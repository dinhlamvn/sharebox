package com.dinhlam.sharebox.listmodel

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelCircleIconBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.updateMargin
import com.dinhlam.sharebox.extensions.updateSize
import com.dinhlam.sharebox.model.Spacing

data class CircleIconListModel(
    val id: String,
    val icon: Drawable?,
    val size: Int = 24.dp(),
    val margin: Spacing = Spacing.None,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<CircleIconListModel, ListModelCircleIconBinding>(
                ListModelCircleIconBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: CircleIconListModel, position: Int) {
                binding.imageIcon.updateSize(model.size, model.size)
                binding.root.updateMargin(model.margin)
                binding.imageIcon.setImageDrawable(model.icon)
                binding.root.setOnClickListener(model.onClick.prop)
            }

            override fun onUnBind() {

            }
        }
    }
}