package com.dinhlam.keepmyshare.ui.home.modelview

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.dinhlam.keepmyshare.R
import com.dinhlam.keepmyshare.base.BaseListAdapter

sealed class HomeItem {


    data class TextItem(
        val id: Long,
        val text: String = ""
    ) : BaseListAdapter.BaseModelView(id) {

        private val textView: TextView by bindView(R.id.text_view)

        override val layoutRes: Int
            get() = R.layout.text_item_view

        override fun onModelBind(viewHolder: BaseListAdapter.BaseViewHolder, position: Int) {
            textView.text = text
        }
    }


    data class ImageItem(
        val id: Long,
        val url: String = ""
    ) : BaseListAdapter.BaseModelView(id) {

        private val imageView: ImageView by bindView(R.id.image_view)

        override val layoutRes: Int
            get() = R.layout.image_item_view

        override fun onModelBind(viewHolder: BaseListAdapter.BaseViewHolder, position: Int) {
            imageView.setImageURI(Uri.parse(url))
        }
    }
}
