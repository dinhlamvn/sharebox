package com.dinhlam.sharebox.modelview

import android.view.View
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
        view: View
    ) : BaseListAdapter.BaseViewHolder<SingleTextModelView, ModelViewSingleTextBinding>(view) {

        override fun onCreateViewBinding(view: View): ModelViewSingleTextBinding {
            return ModelViewSingleTextBinding.bind(view)
        }

        override fun onBind(item: SingleTextModelView, position: Int) {
            binding.root.updateLayoutParams {
                height = item.height
            }
            binding.textView.text = item.text
        }

        override fun onUnBind() {
        }
    }
}
