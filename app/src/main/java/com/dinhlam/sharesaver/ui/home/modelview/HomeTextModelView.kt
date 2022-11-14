package com.dinhlam.sharesaver.ui.home.modelview

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader

data class HomeTextModelView(
    val id: String,
    val iconUrl: String?,
    val content: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_home_share_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeTextModelView && other.id == this.id
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeTextModelView && other == this
    }

    class HomeTextViewHolder(
        view: View,
        private val onClick: (String?) -> Unit,
        private val onShareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<HomeTextModelView, ModelViewHomeShareTextBinding>(view) {

        override fun onBind(item: HomeTextModelView, position: Int) {
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

        override fun onCreateViewBinding(view: View): ModelViewHomeShareTextBinding {
            return ModelViewHomeShareTextBinding.bind(view)
        }
    }
}
