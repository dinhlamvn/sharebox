package com.dinhlam.sharesaver.ui.home.modelview

import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.databinding.ModelViewHomeDateBinding

data class HomeDateModelView(
    val id: String,
    val date: String
) : BaseListAdapter.BaseModelView(id) {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_home_date

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeDateModelView && this.date == other.date
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeDateModelView && this == other
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class HomeDateViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<HomeDateModelView, ModelViewHomeDateBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewHomeDateBinding {
            return ModelViewHomeDateBinding.bind(view)
        }

        override fun onBind(item: HomeDateModelView, position: Int) {
            binding.root.text = item.date
        }

        override fun onUnBind() {
        }
    }
}
