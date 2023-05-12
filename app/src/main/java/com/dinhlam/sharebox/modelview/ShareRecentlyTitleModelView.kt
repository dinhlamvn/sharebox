package com.dinhlam.sharebox.modelview

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareRecentlyTitleBinding

object ShareRecentlyTitleModelView : BaseListAdapter.BaseModelView("share_recently_title") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_recently_title

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareRecentlyTitleViewHolder(
        binding: ModelViewShareRecentlyTitleBinding
    ) : BaseListAdapter.BaseViewHolder<ShareRecentlyTitleModelView, ModelViewShareRecentlyTitleBinding>(
        binding
    ) {

        override fun onBind(model: ShareRecentlyTitleModelView, position: Int) {
        }

        override fun onUnBind() {
        }
    }
}
