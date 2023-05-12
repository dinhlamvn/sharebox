package com.dinhlam.sharebox.modelview.sharelist

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListTextBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.UserUtils

data class ShareListTextModelView(
    val shareId: String,
    val iconUrl: String?,
    val content: String?,
    val createdAt: Long,
    val note: String?,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userDetail: UserDetail
) : BaseListAdapter.BaseModelView(shareId) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_text

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListTextViewHolder(
        view: View,
        private val onClick: (String?) -> Unit,
        private val shareToOther: (String) -> Unit,
        private val actionVote: (String) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListTextModelView, ModelViewShareListTextBinding>(
        view
    ) {

        override fun onBind(model: ShareListTextModelView, position: Int) {
            binding.root.setOnClickListener {
                onClick.invoke(model.content)
            }

            ImageLoader.load(
                context,
                model.userDetail.avatar,
                binding.layoutUserInfo.imageAvatar,
                R.drawable.no_preview_image,
                true
            )

            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }

            binding.layoutBottomAction.buttonUpVote.setOnClickListener {
                actionVote.invoke(model.shareId)
            }

            binding.layoutBottomAction.textUpvote.text =
                context.getString(R.string.up_vote, model.shareUpVote)
            binding.layoutBottomAction.textComment.text =
                context.getString(R.string.comment, model.shareComment)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(context, R.color.colorTextBlack)) {
                    append(model.userDetail.name)
                }
                color(ContextCompat.getColor(context, R.color.colorTextHint)) {
                    append(" shares a text content")
                }
            }
            binding.textShare.text = model.content
            binding.layoutUserInfo.textUserLevel.text =
                UserUtils.getLevelTitle(model.userDetail.level)
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

        override fun onCreateViewBinding(view: View): ModelViewShareListTextBinding {
            return ModelViewShareListTextBinding.bind(view)
        }
    }
}
