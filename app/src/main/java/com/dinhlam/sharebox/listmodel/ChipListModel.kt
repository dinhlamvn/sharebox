package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelChipBinding
import com.dinhlam.sharebox.extensions.getColorCompat

data class ChipListModel(
    val id: String,
    val title: String,
    val isChecked: Boolean,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener>
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<ChipListModel, ListModelChipBinding>(
            ListModelChipBinding.inflate(
                inflater,
                container,
                false
            )
        ) {
            override fun onBind(model: ChipListModel, position: Int) {
                binding.container.setOnClickListener(model.onClick.prop)
                binding.text.text = model.title
                if (model.isChecked) {
                    binding.icon.isVisible = true
                    binding.icon.setIconColor(buildContext.getColorCompat(android.R.color.white))
                    binding.text.setTextColor(buildContext.getColorCompat(android.R.color.white))
                    binding.container.setBackgroundColor(buildContext.getColorCompat(R.color.md_theme_onPrimaryContainer))
                } else {
                    binding.icon.isVisible = false
                    binding.icon.setIconColor(buildContext.getColorCompat(android.R.color.black))
                    binding.text.setTextColor(buildContext.getColorCompat(android.R.color.black))
                    binding.container.setBackgroundColor(buildContext.getColorCompat(R.color.md_theme_surface))
                }
            }

            override fun onUnBind() {

            }
        }
    }
}
