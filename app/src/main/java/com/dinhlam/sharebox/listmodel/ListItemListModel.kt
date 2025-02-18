package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelListItemBinding
import com.dinhlam.sharebox.extensions.asColorInt

data class ListItemListModel(
    val id: String,
    val icon: String,
    val title: String?,
    val subtitle: String?,
    val tagColor: Int? = null,
    val onMore: BaseListAdapter.NoHashProp<OnClickListener>,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener>
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return ListItemViewHolderViewBinding(inflater, container)
    }

    private class ListItemViewHolderViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup
    ) :
        BaseListAdapter.BaseViewHolderViewBinding<ListItemListModel, ListModelListItemBinding>(
            ListModelListItemBinding.inflate(layoutInflater, container, false)
        ) {

        override fun onBind(model: ListItemListModel, position: Int) {
            binding.root.setOnClickListener(model.onClick.prop)
            binding.iconMore.setOnClickListener(model.onMore.prop)
            binding.icon.setIconCode(model.icon)
            binding.textTitle.text = model.title
            binding.textSubtitle.text = model.subtitle

            if (model.tagColor != null) {
                binding.iconTag.isVisible = true
                binding.iconTag.setCardBackgroundColor(model.tagColor.asColorInt())
            } else {
                binding.iconTag.isVisible = false
                binding.iconTag.setCardBackgroundColor(null)
            }
        }
    }
}
