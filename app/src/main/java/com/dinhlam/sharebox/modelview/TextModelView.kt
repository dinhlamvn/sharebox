package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextBinding

data class TextModelView(
    val text: String,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT
) : BaseListAdapter.BaseModelView("text_$text") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<TextModelView, ModelViewTextBinding>(
            ModelViewTextBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: TextModelView, position: Int) {
                binding.root.updateLayoutParams {
                    height = model.height
                }
                binding.textView.text = model.text
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
