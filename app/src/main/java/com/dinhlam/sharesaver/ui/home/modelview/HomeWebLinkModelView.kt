package com.dinhlam.sharesaver.ui.home.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareWebLinkBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader

data class HomeWebLinkModelView(
    val id: String,
    val iconUrl: String?,
    val url: String?,
    val createdAt: Long,
    val note: String?,
    val showDivider: Boolean = true
) : BaseListAdapter.BaseModelView(id) {

    override val layoutRes: Int
        get() = R.layout.model_view_home_share_web_link

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeWebLinkModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeWebLinkModelView && other == this
    }

    class HomeTextViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<HomeWebLinkModelView, ModelViewHomeShareWebLinkBinding>(view) {

        override fun onBind(item: HomeWebLinkModelView, position: Int) {
            binding.root.setOnClickListener {
                startView(item)
            }
            ImageLoader.load(
                context, item.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewUrl.text = item.url
            binding.textViewCreatedDate.text = item.createdAt.format("H:mm")
            binding.textViewNote.isVisible = !item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
            binding.viewDivider.isInvisible = !item.showDivider
        }

        override fun onUnBind() {

        }

        override fun onCreateViewBinding(view: View): ModelViewHomeShareWebLinkBinding {
            return ModelViewHomeShareWebLinkBinding.bind(view)
        }

        private fun startView(item: HomeWebLinkModelView) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
