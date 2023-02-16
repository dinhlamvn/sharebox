package com.dinhlam.sharebox.modelview.sharelist

import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListTextBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader

data class ShareListTextModelView(
    val id: String,
    val iconUrl: String?,
    val content: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_text

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListTextViewHolder(
        view: View,
        private val onClick: (String?) -> Unit,
        private val onShareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListTextModelView, ModelViewShareListTextBinding>(
        view
    ) {

        override fun onBind(model: ShareListTextModelView, position: Int) {
            binding.imageOption.setOnClickListener {
                onShareToOther.invoke(model.shareId)
            }

            binding.root.setOnClickListener {
                onClick.invoke(model.content)
            }

            ImageLoader.load(
                context, model.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewContent.text = model.content
            binding.textViewCreatedDate.text = model.createdAt.format("MMM d h:mm a")
            binding.textViewNote.text = model.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListTextBinding {
            return ModelViewShareListTextBinding.bind(view)
        }
    }
}
