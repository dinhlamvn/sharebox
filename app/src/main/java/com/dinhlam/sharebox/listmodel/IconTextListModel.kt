package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ListModelIconTextBinding
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat

data class IconTextListModel(
    val id: String,
    val icon: String,
    val text: String,
    val textAppearance: Int = R.style.TextBody,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<IconTextListModel, ListModelIconTextBinding>(
                ListModelIconTextBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: IconTextListModel, position: Int) {
                binding.root.setOnClickListener(model.actionClick.prop)
                binding.icon.setIconCode(model.icon)
                binding.text.setTextAppearanceCompat(model.textAppearance)
                binding.text.text = model.text
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
