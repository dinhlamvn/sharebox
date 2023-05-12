package com.dinhlam.sharebox.modelview.sharelist

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListUrlBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.utils.UserUtils
import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

data class ShareListUrlModelView(
    val id: String,
    val iconUrl: String?,
    val url: String?,
    val createdAt: Long,
    val note: String?,
    val shareId: Int,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userAvatar: String = "",
    val userName: String = "",
    val userLevel: Int = 0,
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_url

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListWebLinkWebHolder(
        view: View,
        private val openAction: (String) -> Unit,
        private val shareToOther: (Int) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListUrlModelView, ModelViewShareListUrlBinding>(
        view
    ) {

        override fun onBind(model: ShareListUrlModelView, position: Int) {
            binding.container.setOnClickListener {
                openAction(model.url!!)
            }
            ImageLoader.load(
                context,
                model.userAvatar,
                binding.layoutUserInfo.imageAvatar,
                circle = true
            )
            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }

            binding.layoutBottomAction.textUpvote.text =
                context.getString(R.string.up_vote, model.shareUpVote)
            binding.layoutBottomAction.textComment.text =
                context.getString(R.string.comment, model.shareComment)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(context, R.color.colorTextBlack)) {
                    append(model.userName)
                }
                color(ContextCompat.getColor(context, R.color.colorTextHint)) {
                    append(" shares a weblink")
                }
            }
            binding.shareLinkPreview.setLink(model.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 1, maxLineTitle = 1)
            }
            binding.layoutUserInfo.textUserLevel.text = UserUtils.getLevelTitle(model.userLevel)

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
            binding.shareLinkPreview.resetUi()
            binding.textViewNote.text = null
        }

        override fun onCreateViewBinding(view: View): ModelViewShareListUrlBinding {
            return ModelViewShareListUrlBinding.bind(view)
        }
    }
}
