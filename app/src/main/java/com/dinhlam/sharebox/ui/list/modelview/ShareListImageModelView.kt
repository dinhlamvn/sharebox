package com.dinhlam.sharebox.ui.list.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isInvisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListImageBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareListImageModelView(
    val id: String,
    val uri: Uri,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_image

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListImageModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListImageModelView && other === this
    }

    class ShareListImageViewHolder(view: View, private val block: (Int) -> Unit) :
        BaseListAdapter.BaseViewHolder<ShareListImageModelView, ModelViewShareListImageBinding>(
            view
        ) {

        override fun onBind(item: ShareListImageModelView, position: Int) {
            binding.imageShare.setOnClickListener {
                block.invoke(item.shareId)
            }
            binding.imageShareContent.setOnClickListener {
                startViewImage(item.uri)
            }
            ImageLoader.load(context, item.uri, binding.imageShareContent)
            binding.textViewCreatedDate.text = item.createdAt.format("HH:mm")
            binding.textViewNote.isInvisible = item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListImageBinding {
            return ModelViewShareListImageBinding.bind(view)
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
