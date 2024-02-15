package com.dinhlam.sharebox.listmodel

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.GravityInt
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewIconTextBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat

data class IconTextListModel(
    val id: String,
    @DrawableRes val icon: Int,
    val text: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val textAppearance: Int = R.style.TextBody,
    @GravityInt val gravity: Int = Gravity.CENTER_VERTICAL,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<IconTextListModel, ModelViewIconTextBinding>(
            ModelViewIconTextBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: IconTextListModel, position: Int) {
                binding.root.updateLayoutParams {
                    width = model.width
                    height = model.height
                }
                binding.textView.gravity = model.gravity
                binding.textView.setTextAppearanceCompat(model.textAppearance)
                binding.textView.text = model.text
                model.actionClick.prop?.let { listener ->
                    binding.textView.setOnClickListener(listener)
                }
                binding.textView.setDrawableCompat(start = model.icon)
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
