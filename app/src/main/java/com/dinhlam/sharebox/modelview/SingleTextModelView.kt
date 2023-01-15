package com.dinhlam.sharebox.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewSingleTextBinding

data class SingleTextModelView(val text: String) :
    BaseListAdapter.BaseModelView("single_text_$text") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_single_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is SingleTextModelView
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is SingleTextModelView
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class SingleTextViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<SingleTextModelView, ModelViewSingleTextBinding>(view) {

        override fun onCreateViewBinding(view: View): ModelViewSingleTextBinding {
            return ModelViewSingleTextBinding.bind(view)
        }

        override fun onBind(item: SingleTextModelView, position: Int) {
            binding.textView.text = item.text
        }

        override fun onUnBind() {
        }
    }
}
