package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelTagItemBinding
import com.dinhlam.sharebox.extensions.asColorInt
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.ifTrue

data class TagItemListModel(
    val id: String,
    val tagColor: Int,
    val isSelected: Boolean,
    val onClickListener: BaseListAdapter.NoHashProp<OnClickListener>
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<TagItemListModel, ListModelTagItemBinding>(
                ListModelTagItemBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: TagItemListModel, position: Int) {
                binding.root.setOnClickListener(model.onClickListener.prop)
                binding.tag.setCardBackgroundColor(model.tagColor.asColorInt())
                binding.tag.strokeWidth = model.isSelected.ifTrue(3.dp, 0)
                binding.iconCheck.isVisible = model.isSelected
            }
        }
    }
}
