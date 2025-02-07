package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelListItemBinding
import com.dinhlam.sharebox.utils.Icons
import com.mikepenz.iconics.typeface.IIcon

data class ListItemListModel(
    val id: String,
    val icon: IIcon,
    val title: String?,
    val subtitle: String?,
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

        init {
            binding.imageAction.setImageDrawable(Icons.moreIcon(buildContext))
        }

        override fun onBind(model: ListItemListModel, position: Int) {
            binding.root.setOnClickListener(model.onClick.prop)
            binding.imageAction.setOnClickListener(model.onMore.prop)
            binding.imageIcon.setImageDrawable(Icons.icon(buildContext, model.icon))
            binding.textTitle.text = model.title
            binding.textSubtitle.text = model.subtitle
        }

        override fun onUnBind() {

        }
    }
}
