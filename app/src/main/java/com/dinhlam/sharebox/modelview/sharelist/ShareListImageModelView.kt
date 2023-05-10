package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListImageBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.utils.IconUtils

data class ShareListImageModelView(
    val id: String, val uri: Uri, val createdAt: Long, val note: String?, val shareId: Int
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListImageViewHolder(
        view: View, private val shareToOther: (Int) -> Unit, private val viewImage: (Uri) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListImageModelView, ModelViewShareListImageBinding>(
        view
    ) {

        override fun onBind(model: ShareListImageModelView, position: Int) {
            binding.root.setOnClickListener {
                viewImage(model.uri)
            }
            ImageLoader.load(
                context,
                IconUtils.FAKE_AVATAR,
                binding.layoutUserInfo.imageAvatar,
                R.drawable.no_preview_image,
                true
            )
            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }
            ImageLoader.load(context, model.uri, binding.imageShare)
            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(context, R.color.colorTextBlack)) {
                    append("William Smith")
                }
                color(ContextCompat.getColor(context, R.color.colorTextHint)) {
                    append(" shares an image")
                }
            }
            binding.layoutUserInfo.textUserLevel.text = "Newbie"
            binding.textCreatedDate.text = model.createdAt.formatForFeed()
            model.note.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.text = text
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListImageBinding {
            return ModelViewShareListImageBinding.bind(view)
        }
    }
}
