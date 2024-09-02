package com.dinhlam.sharebox.listmodel

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.GravityInt
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat
import com.dinhlam.sharebox.extensions.updatePadding
import com.dinhlam.sharebox.extensions.updateSize
import com.dinhlam.sharebox.model.Spacing

data class TextListModel(
    val id: String,
    val text: String,
    @ColorRes val textColor: Int = 0,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val textAppearance: Int = R.style.TextBody,
    @GravityInt val gravity: Int = Gravity.CENTER,
    val startIcon: Drawable? = null,
    val endIcon: Drawable? = null,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),
    val padding: Spacing = Spacing.Horizontal(16.dp(), 16.dp()),
) : BaseListAdapter.BaseListModel("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<TextListModel, ModelViewTextBinding>(
            ModelViewTextBinding.inflate(inflater, container, false)
        ) {

            private val defaultTextColor = binding.textView.currentTextColor

            override fun onBind(model: TextListModel, position: Int) {
                binding.root.updateSize(model.width, model.height)
                binding.root.updatePadding(model.padding)
                binding.textView.gravity = model.gravity
                binding.textView.setTextAppearanceCompat(model.textAppearance)
                binding.textView.text = model.text
                binding.textView.setOnClickListener(model.actionClick.prop)

                binding.textView.setDrawableCompat(start = model.startIcon, end = model.endIcon)

                model.textColor.takeIf { it != 0 }?.let { textColor ->
                    binding.textView.setTextColor(ContextCompat.getColor(buildContext, textColor))
                } ?: apply {
                    binding.textView.setTextColor(defaultTextColor)
                }
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
