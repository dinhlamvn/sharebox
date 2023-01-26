package com.dinhlam.sharebox.ui.share.modelview

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewShareDefaultBinding

class ShareDefaultModelView : BaseListAdapter.BaseModelView("share_default") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_default

    class ShareDefaultViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<BaseListAdapter.BaseModelView, ModelViewShareDefaultBinding>(
            view
        ) {
        override fun onCreateViewBinding(view: View): ModelViewShareDefaultBinding {
            return ModelViewShareDefaultBinding.bind(view)
        }

        override fun onBind(item: BaseListAdapter.BaseModelView, position: Int) {
        }

        override fun onUnBind() {
        }
    }
}
