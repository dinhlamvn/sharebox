package com.dinhlam.sharebox.ui.home.modelview.recently

import android.content.Intent
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

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareRecentlyImageModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareRecentlyImageModelView && other === this
    }

    class ShareRecentlyImageViewHolder(view: View, private val block: (Int) -> Unit) :
        BaseListAdapter.BaseViewHolder<ShareRecentlyImageModelView, ModelViewShareRecentlyImageBinding>(
            view
        ) {

        override fun onBind(item: ShareRecentlyImageModelView, position: Int) {
            binding.imageShare.setOnClickListener {
                block.invoke(item.shareId)
            }
            binding.imageShareContent.setOnClickListener {
                startViewImage(item.uri)
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

        private fun startViewImage(uri: Uri) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "image/*")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            context.startActivity(intent)
        }
    }
}
