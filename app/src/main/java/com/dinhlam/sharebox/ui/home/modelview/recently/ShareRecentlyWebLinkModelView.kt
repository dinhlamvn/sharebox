package com.dinhlam.sharebox.ui.home.modelview.recently

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareRecentlyWebLinkBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareRecentlyWebLinkModelView(
    val id: String,
    val iconUrl: String?,
    val url: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_recently_web_link

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareRecentlyWebLinkWebHolder(
        view: View,
        private val openAction: (Int) -> Unit,
        private val shareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareRecentlyWebLinkModelView, ModelViewShareRecentlyWebLinkBinding>(
        view
    ) {

        override fun onBind(item: ShareRecentlyWebLinkModelView, position: Int) {
            binding.root.setOnClickListener {
                openAction.invoke(position)
            }
            binding.imageShare.setOnClickListener {
                shareToOther(item.shareId)
            }
            ImageLoader.load(
                context,
                item.iconUrl,
                binding.imageView,
                R.drawable.ic_share_text,
                true
            )
            binding.textViewUrl.text = item.url
            binding.textViewCreatedDate.text = item.createdAt.format("MMM d h:mm a")
            binding.textViewNote.isVisible = !item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareRecentlyWebLinkBinding {
            return ModelViewShareRecentlyWebLinkBinding.bind(view)
        }
    }
}
