package com.dinhlam.sharebox.listmodel

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.GravityInt
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat

data class TextListModel(
    val id: String,
    val text: String,
    @ColorRes val textColor: Int = 0,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val textAppearance: Int = R.style.TextAppearance_MaterialComponents_Body2,
    @GravityInt val gravity: Int = Gravity.CENTER,
    val endIcon: Drawable? = null,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<TextListModel, ModelViewTextBinding>(
            ModelViewTextBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: TextListModel, position: Int) {
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

                binding.textView.setDrawableCompat(end = model.endIcon)

                model.textColor.takeIf { it != 0 }?.let { textColor ->
                    binding.textView.setTextColor(ContextCompat.getColor(buildContext, textColor))
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
