package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextBinding
import com.dinhlam.sharebox.extensions.setTextAppearanceCompat

data class TextModelView(
    val id: String,
    val text: String,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val textAppearance: Int = R.style.TextAppearance_Body,
    val actionClick: OnClickListener? = null,
) : BaseListAdapter.BaseModelView("text_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<TextModelView, ModelViewTextBinding>(
            ModelViewTextBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: TextModelView, position: Int) {
                binding.root.updateLayoutParams {
                    width = model.width
                    height = model.height
                }
                binding.textView.setTextAppearanceCompat(model.textAppearance)
                binding.textView.text = model.text
                binding.textView.setOnClickListener(model.actionClick)
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
