package com.dinhlam.sharesaver.ui.home.modelview

import android.net.Uri
import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ImageItemViewBinding
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.loader.ImageLoader
import java.text.SimpleDateFormat
import java.util.Locale

sealed class HomeItemModelView {

    data class HomeTextModelView(
        val id: String,
        val text: String = "",
        val createdAt: Long
    ) : BaseListAdapter.BaseModelView(id) {

        override val layoutRes: Int
            get() = R.layout.model_view_home_share_text

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is HomeTextModelView && other == this
        }

        class HomeTextViewHolder(view: View) :
            BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeTextModelView, ModelViewHomeShareTextBinding>(
                view
            ) {

            private val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

            override fun onBind(item: HomeItemModelView.HomeTextModelView, position: Int) {
                binding.textView.text = item.text
                binding.textViewCreatedDate.text = df.format(item.createdAt)
            }

            override fun onUnBind() {

            }

            override fun onCreateViewBinding(view: View): ModelViewHomeShareTextBinding {
                return ModelViewHomeShareTextBinding.bind(view)
            }
        }
    }


    data class HomeImageModelView(
        val id: String,
        val uri: Uri
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
            BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeImageModelView, ImageItemViewBinding>(view) {

            override fun onBind(item: HomeItemModelView.HomeImageModelView, position: Int) {
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
