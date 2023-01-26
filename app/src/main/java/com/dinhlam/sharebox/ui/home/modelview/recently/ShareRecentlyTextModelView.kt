package com.dinhlam.sharebox.ui.home.modelview.recently

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareRecentlyTextBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareRecentlyTextModelView(
    val id: String,
    val iconUrl: String?,
    val content: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_recently_text

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareRecentlyTextViewHolder(
        view: View,
        private val onClick: (String?) -> Unit,
        private val onShareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareRecentlyTextModelView, ModelViewShareRecentlyTextBinding>(
        view
    ) {

        override fun onBind(item: ShareRecentlyTextModelView, position: Int) {
            binding.imageShare.setOnClickListener {
                onShareToOther.invoke(item.shareId)
            }

            binding.root.setOnClickListener {
                onClick.invoke(item.content)
            }

            ImageLoader.load(
                context, item.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewContent.text = item.content
            binding.textViewCreatedDate.text = item.createdAt.format("MMM d h:mm a")
            binding.textViewNote.isVisible = !item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareRecentlyTextBinding {
            return ModelViewShareRecentlyTextBinding.bind(view)
        }
    }
}
