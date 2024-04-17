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
import com.dinhlam.sharebox.databinding.ListModelTextPairBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat
import com.dinhlam.sharebox.extensions.updatePadding
import com.dinhlam.sharebox.extensions.updateSize
import com.dinhlam.sharebox.model.Spacing

data class TextPairListModel(
    val id: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val padding: Spacing = Spacing.Horizontal(16.dp(), 16.dp()),

    val text1: String? = null,
    @ColorRes val textColor1: Int = 0,
    val textAppearance1: Int = R.style.TextBody,
    @GravityInt val gravity1: Int = Gravity.START,
    val endIcon1: Drawable? = null,
    val actionClick1: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),

    val text2: String? = null,
    @ColorRes val textColor2: Int = 0,
    val textAppearance2: Int = R.style.TextBody,
    @GravityInt val gravity2: Int = Gravity.END,
    val endIcon2: Drawable? = null,
    val actionClick2: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),

    ) : BaseListAdapter.BaseListModel("text_pair_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<TextPairListModel, ListModelTextPairBinding>(
            ListModelTextPairBinding.inflate(inflater, container, false)
        ) {

            private val defaultTextColor1 = binding.textView1.currentTextColor
            private val defaultTextColor2 = binding.textView1.currentTextColor

            override fun onBind(model: TextPairListModel, position: Int) {
                binding.root.updateSize(model.width, model.height)
                binding.root.updatePadding(model.padding)

                binding.textView1.gravity = model.gravity1
                binding.textView1.setTextAppearanceCompat(model.textAppearance1)
                binding.textView1.text = model.text1
                binding.textView1.setOnClickListener(model.actionClick1.prop)

                binding.textView1.setDrawableCompat(end = model.endIcon1)

                model.textColor1.takeIf { it != 0 }?.let { textColor ->
                    binding.textView1.setTextColor(ContextCompat.getColor(buildContext, textColor))
                } ?: apply { binding.textView1.setTextColor(defaultTextColor1) }

                binding.textView2.gravity = model.gravity2
                binding.textView2.setTextAppearanceCompat(model.textAppearance2)
                binding.textView2.text = model.text2
                binding.textView2.setOnClickListener(model.actionClick2.prop)

                binding.textView2.setDrawableCompat(end = model.endIcon2)

                model.textColor2.takeIf { it != 0 }?.let { textColor ->
                    binding.textView2.setTextColor(ContextCompat.getColor(buildContext, textColor))
                } ?: apply { binding.textView2.setTextColor(defaultTextColor2) }
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
