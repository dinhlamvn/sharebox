package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewBoxBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.utils.Icons

data class BoxListModel(
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
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<BoxListModel, ModelViewBoxBinding>(
            ModelViewBoxBinding.inflate(inflater, container, false)
        ) {

            init {
                binding.imageIcon.setImageDrawable(Icons.boxIcon(buildContext) {
                    copy(sizeDp = 32, colorRes = R.color.md_theme_primary)
                })
                binding.imageAction.setImageDrawable(Icons.moreIcon(buildContext))
            }

            override fun onBind(model: BoxListModel, position: Int) {
                binding.imageAction.isVisible = model.isShowOptionAction
                binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = model.margin.start
                    topMargin = model.margin.top
                    marginEnd = model.margin.end
                    bottomMargin = model.margin.bottom
                }

                binding.imageAction.setOnClickListener(model.onOptionClick.prop)

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
