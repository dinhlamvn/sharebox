package com.dinhlam.sharebox.ui.home.modelview.recently

import android.net.Uri
import android.view.View
import androidx.core.view.isInvisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareRecentlyImageBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareRecentlyImageModelView(
    val id: String, val uri: Uri, val createdAt: Long, val note: String?, val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_recently_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareRecentlyImageViewHolder(
        view: View, private val block: (Int) -> Unit, private val blockViewImage: (Uri) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareRecentlyImageModelView, ModelViewShareRecentlyImageBinding>(
        view
    ) {

        override fun onBind(item: ShareRecentlyImageModelView, position: Int) {
            binding.imageShare.setOnClickListener {
                block.invoke(item.shareId)
            }
            binding.root.setOnClickListener {
                blockViewImage(item.uri)
            }
            ImageLoader.load(context, item.uri, binding.imageShareContent)
            binding.textViewCreatedDate.text = item.createdAt.format("MMM d h:mm a")
            binding.textViewNote.isInvisible = item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareRecentlyImageBinding {
            return ModelViewShareRecentlyImageBinding.bind(view)
        }
    }
}
