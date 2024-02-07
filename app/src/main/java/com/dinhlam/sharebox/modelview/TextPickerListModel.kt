package com.dinhlam.sharebox.modelview

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextPickerBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat

data class TextPickerListModel(
    val id: String,
    val text: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val textAppearance: Int = R.style.TextAppearance_MaterialComponents_Body2,
    val isPicked: Boolean = false,
    val startIcon: Drawable? = null,
    val pickedIcon: Drawable? = null,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<TextPickerListModel, ModelViewTextPickerBinding>(
                ModelViewTextPickerBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: TextPickerListModel, position: Int) {
                binding.root.updateLayoutParams {
                    width = model.width
                    height = model.height
                }
                binding.textView.setTextAppearanceCompat(model.textAppearance)
                binding.textView.text = model.text
                model.actionClick.prop?.let { listener ->
                    binding.textView.setOnClickListener(listener)
                }
                binding.textView.setDrawableCompat(
                    start = model.startIcon,
                    end = if (model.isPicked) model.pickedIcon else null
                )
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
