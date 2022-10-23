package com.dinhlam.sharesaver.ui.home.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isInvisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareImageBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader

data class HomeImageModelView(
    val id: String, val uri: Uri, val createdAt: Long, val note: String?, val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_home_share_image

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeImageModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeImageModelView && other == this
    }

    class HomeImageViewHolder(view: View, private val block: (Int) -> Unit) :
        BaseListAdapter.BaseViewHolder<HomeImageModelView, ModelViewHomeShareImageBinding>(
            view
        ) {

        override fun onBind(item: HomeImageModelView, position: Int) {
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

        override fun onCreateViewBinding(view: View): ModelViewHomeShareImageBinding {
            return ModelViewHomeShareImageBinding.bind(view)
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
