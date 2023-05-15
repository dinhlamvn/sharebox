package com.dinhlam.sharebox.modelview

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewTextBinding

data class TextModelView(
    val text: String,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT
) :
    BaseListAdapter.BaseModelView("text_$text") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_text

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class TextViewHolder(
        private val binding: ModelViewTextBinding,
    ) : BaseListAdapter.BaseViewHolder<TextModelView, ModelViewTextBinding>(binding) {

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
