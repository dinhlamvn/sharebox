package com.dinhlam.sharesaver.ui.home.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader

data class HomeTextModelView(
    val id: String,
    val iconUrl: String?,
    val text: String?,
    val createdAt: Long,
    val note: String?,
    val showDivider: Boolean = true
) : BaseListAdapter.BaseModelView(id) {

    override val layoutRes: Int
        get() = R.layout.model_view_home_share_text

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeTextModelView && other.id == this.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeTextModelView && other == this
    }

    class HomeTextViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<HomeTextModelView, ModelViewHomeShareTextBinding>(view) {

        override fun onBind(item: HomeTextModelView, position: Int) {
            binding.root.setOnClickListener {
                startView(item)
            }
            ImageLoader.load(
                context, item.iconUrl, binding.imageView, R.drawable.ic_share_text, true
            )
            binding.textViewShareContent.text = item.text
            binding.textViewCreatedDate.text = item.createdAt.format("H:mm")
            binding.textViewNote.isVisible = !item.note.isNullOrBlank()
            binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
            binding.viewDivider.isInvisible = !item.showDivider
        }

        override fun onUnBind() {

        }

        override fun onCreateViewBinding(view: View): ModelViewHomeShareTextBinding {
            return ModelViewHomeShareTextBinding.bind(view)
        }

        private fun startView(item: HomeTextModelView) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.text))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
