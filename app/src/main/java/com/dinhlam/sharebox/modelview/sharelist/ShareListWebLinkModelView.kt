package com.dinhlam.sharebox.modelview.sharelist

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListWebLinkBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.formatForFeed
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
        view: View,
        private val openAction: (String) -> Unit,
        private val shareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListWebLinkModelView, ModelViewShareListWebLinkBinding>(
        view
    ) {

        override fun onBind(model: ShareListWebLinkModelView, position: Int) {
            binding.container.setOnClickListener {
                openAction(model.url!!)
            }
            ImageLoader.load(
                context,
                "https://i.pravatar.cc/300",
                binding.layoutUserInfo.imageAvatar,
                circle = true
            )
            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }
            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(context, R.color.colorTextBlack)) {
                    append("William Smith")
                }
                color(ContextCompat.getColor(context, R.color.colorTextHint)) {
                    append(" shares a weblink")
                }
            }
            binding.shareLinkPreview.setLink(model.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 1, maxLineTitle = 1)
            }
            binding.layoutUserInfo.textUserLevel.text = "Senior Member"

            binding.textCreatedDate.text = model.createdAt.formatForFeed()

            model.note.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.text = text
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            binding.shareLinkPreview.resetUi()
            binding.textViewNote.text = null
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListWebLinkBinding {
            return ModelViewShareListWebLinkBinding.bind(view)
        }
    }
}
