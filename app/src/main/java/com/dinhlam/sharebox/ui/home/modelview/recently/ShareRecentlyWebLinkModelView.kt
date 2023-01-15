package com.dinhlam.sharebox.ui.home.modelview.recently

import android.content.Intent
import android.net.Uri
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

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareRecentlyWebLinkModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareRecentlyWebLinkModelView && other === this
    }

    class ShareRecentlyWebLinkWebHolder(
        view: View,
        val shareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareRecentlyWebLinkModelView, ModelViewShareRecentlyWebLinkBinding>(
        view
    ) {

        override fun onBind(item: ShareRecentlyWebLinkModelView, position: Int) {
            binding.root.setOnClickListener {
                startView(item)
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

        private fun startView(item: ShareRecentlyWebLinkModelView) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
