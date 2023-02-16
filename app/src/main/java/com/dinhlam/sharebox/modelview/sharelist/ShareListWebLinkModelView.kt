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

        override fun onBind(model: ShareListWebLinkModelView, position: Int) {
            binding.container.setOnClickListener {
                openAction.invoke(model.shareId)
            }
            binding.imageOption.setOnClickListener {
                shareToOther(model.shareId)
            }
            ImageLoader.load(
                context, model.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewUrl.setLink(model.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 2, maxLineTitle = 1)
            }
            binding.textViewCreatedDate.text = model.createdAt.format("MMM d h:mm a")
            binding.textViewNote.text = model.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListWebLinkBinding {
            return ModelViewShareListWebLinkBinding.bind(view)
        }
    }
}
