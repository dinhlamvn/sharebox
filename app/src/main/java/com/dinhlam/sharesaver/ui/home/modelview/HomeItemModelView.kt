package com.dinhlam.sharesaver.ui.home.modelview

import android.content.Intent
import android.net.Uri
import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ImageItemViewBinding
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.loader.ImageLoader

sealed class HomeItemModelView {

    data class HomeTextModelView(
        val id: String, val text: String = "", val createdAt: Long
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
        val id: String, val uri: Uri
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.image_item_view

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeImageModelView && other == this
        }

        class HomeImageViewHolder(view: View) :
            BaseListAdapter.BaseViewHolder<HomeImageModelView, ImageItemViewBinding>(
                view
            ) {

            override fun onBind(item: HomeImageModelView, position: Int) {
                ImageLoader.load(context, item.uri, binding.imageView)
            }

            override fun onUnBind() {

            }

            override fun onCreateViewBinding(view: View): ImageItemViewBinding {
                return ImageItemViewBinding.bind(view)
            }
        }
    }
}
