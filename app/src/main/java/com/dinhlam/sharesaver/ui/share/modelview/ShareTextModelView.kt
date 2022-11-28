package com.dinhlam.sharesaver.ui.share.modelview

import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ShareItemTextBinding

data class ShareTextModelView(val id: String, val text: String?) : BaseListAdapter.BaseModelView("share_text_$id") {
    override val modelLayoutRes: Int
        get() = R.layout.share_item_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareTextModelView && other == this
    }

    class ShareTextViewHolder(view: View) :
        BaseListAdapter.BaseViewHolder<ShareTextModelView, ShareItemTextBinding>(view) {
        override fun onCreateViewBinding(view: View): ShareItemTextBinding {
            return ShareItemTextBinding.bind(view)
        }

        override fun onBind(item: ShareTextModelView, position: Int) {
            binding.textView.text = item.text
        }

        override fun onUnBind() {
        }
    }
}
