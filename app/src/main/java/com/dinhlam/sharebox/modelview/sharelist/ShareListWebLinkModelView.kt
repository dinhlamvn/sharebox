package com.dinhlam.sharebox.modelview.sharelist

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListWebLinkBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

data class ShareListWebLinkModelView(
    val id: String,
    val iconUrl: String?,
    val url: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_web_link

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListWebLinkWebHolder(
        view: View, private val openAction: (Int) -> Unit, private val shareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListWebLinkModelView, ModelViewShareListWebLinkBinding>(
        view
    ) {

        override fun onBind(item: ShareListWebLinkModelView, position: Int) {
            binding.container.setOnClickListener {
                openAction.invoke(item.shareId)
            }
            binding.imageOption.setOnClickListener {
                shareToOther(item.shareId)
            }
            ImageLoader.load(
                context, item.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewUrl.setLink(item.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 2, maxLineTitle = 1)
            }
            binding.textViewCreatedDate.text = item.createdAt.format("MMM d h:mm a")
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListWebLinkBinding {
            return ModelViewShareListWebLinkBinding.bind(view)
        }
    }
}
