package com.dinhlam.sharebox.ui.list.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListDateBinding

data class ShareListDateModelView(
    val id: String,
    val date: String
) : BaseListAdapter.BaseModelView(id) {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_date

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListDateModelView && this.date == other.date
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListDateModelView && this === other
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListDateViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareListDateModelView, ModelViewShareListDateBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewShareListDateBinding {
            return ModelViewShareListDateBinding.bind(view)
        }

        override fun onBind(item: ShareListDateModelView, position: Int) {
            binding.root.text = item.date
        }

        override fun onUnBind() {
        }
    }
}
