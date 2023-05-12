package com.dinhlam.sharebox.modelview

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewSingleTextBinding

data class SingleTextModelView(
    val text: String,
    val height: Int = ViewGroup.LayoutParams.MATCH_PARENT
) :
    BaseListAdapter.BaseModelView("single_text_$text") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_single_text

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class SingleTextViewHolder(
        private val binding: ModelViewSingleTextBinding,
    ) : BaseListAdapter.BaseViewHolder<SingleTextModelView, ModelViewSingleTextBinding>(binding) {

        override fun onBind(model: SingleTextModelView, position: Int) {
            binding.root.updateLayoutParams {
                height = model.height
            }
            binding.textView.text = model.text
        }

        override fun onUnBind() {
        }
    }
}
