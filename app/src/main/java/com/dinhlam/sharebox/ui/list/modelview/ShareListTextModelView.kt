package com.dinhlam.sharebox.ui.list.modelview

import android.view.View
import androidx.core.view.isVisible
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

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListTextModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is ShareListTextModelView && other === this
    }

    class ShareListTextViewHolder(
        view: View,
        private val onClick: (String?) -> Unit,
        private val onShareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListTextModelView, ModelViewShareListTextBinding>(view) {

        override fun onBind(item: ShareListTextModelView, position: Int) {
            binding.imageShare.setOnClickListener {
                onShareToOther.invoke(item.shareId)
            }

            binding.root.setOnClickListener {
                onClick.invoke(item.content)
            }

            ImageLoader.load(
                context,
                item.iconUrl,
                binding.imageView,
                R.drawable.ic_share_text,
                true
            )
            binding.textViewContent.text = item.content
            binding.textViewCreatedDate.text = item.createdAt.format("H:mm")
            binding.textViewNote.isVisible = !item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListTextBinding {
            return ModelViewShareListTextBinding.bind(view)
        }
    }
}