package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelBoxItemBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.utils.Icons

data class BoxItemListModel(
    val id: String,
    val boxId: String,
    val name: String,
    val created: Long,
    val margin: Spacing = Spacing.None,
    val hasPasscode: Boolean = false,
    val isShowOptionAction: Boolean = false,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener?> = BaseListAdapter.NoHashProp(null),
    val onOptionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<BoxItemListModel, ListModelBoxItemBinding>(
                ListModelBoxItemBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: BoxItemListModel, position: Int) {
                binding.iconMore.isVisible = model.isShowOptionAction
                binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = model.margin.start
                    topMargin = model.margin.top
                    marginEnd = model.margin.end
                    bottomMargin = model.margin.bottom
                }

                binding.iconMore.setOnClickListener(model.onOptionClick.prop)

                binding.container.setOnClickListener(model.onClick.prop)
                binding.textName.text = model.name
                binding.textName.setDrawableCompat(
                    if (model.hasPasscode) Icons.lockIcon(
                        buildContext
                    ) { copy(sizeDp = 12) } else null)
                binding.textCreatedDate.text = model.created.format("yyyy MMM d HH:mm")
            }

            override fun onUnBind() {

            }
        }
    }
}
