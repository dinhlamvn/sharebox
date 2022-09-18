package com.dinhlam.sharesaver.ui.home.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareImageBinding
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader

sealed class HomeItemModelView {

    data class HomeTextModelView(
        val id: String, val text: String = "", val createdAt: Long, val note: String? = ""
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
                binding.textViewShareContent.text = item.text
                binding.textViewCreatedDate.text = item.createdAt.format("yyyy-MM-dd H:mm")
                binding.textViewNote.isVisible = !item.note.isNullOrBlank()
                binding.textViewNote.text = item.note.takeIfNotNullOrBlank()
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


    data class HomeImageModelView(
        val id: String,
        val uri: Uri,
        val createdAt: Long,
        val note: String?
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.model_view_home_share_image

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other == this
        }

        class HomeImageViewHolder(view: View) :
            BaseListAdapter.BaseViewHolder<HomeImageModelView, ModelViewHomeShareImageBinding>(
                view
            ) {

            override fun onBind(item: HomeImageModelView, position: Int) {
                binding.imageShareContent.setOnClickListener {
                    startViewImage(item.uri)
                }
                ImageLoader.load(context, item.uri, binding.imageShareContent)
                binding.textViewCreatedDate.text = item.createdAt.format("yyyy-MM-dd H:mm")
                binding.textViewNote.isVisible = !item.note.isNullOrBlank()
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
                context.startActivity(Intent.createChooser(intent, "Choose to view"))
            }
        }
    }
}
