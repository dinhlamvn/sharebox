package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListImageBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareListImageModelView(
    val id: String, val uri: Uri, val createdAt: Long, val note: String?, val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListImageViewHolder(
        view: View, private val block: (Int) -> Unit, private val blockViewImage: (Uri) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListImageModelView, ModelViewShareListImageBinding>(
        view
    ) {

        override fun onBind(model: ShareListImageModelView, position: Int) {
            binding.imageOption.setOnClickListener {
                block.invoke(model.shareId)
            }
            binding.root.setOnClickListener {
                blockViewImage(model.uri)
            }
            ImageLoader.load(context, model.uri, binding.imageShareContent)
            binding.textViewCreatedDate.text = model.createdAt.format("MMM d h:mm a")
            binding.textViewNote.text = model.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListImageBinding {
            return ModelViewShareListImageBinding.bind(view)
        }
    }
}
