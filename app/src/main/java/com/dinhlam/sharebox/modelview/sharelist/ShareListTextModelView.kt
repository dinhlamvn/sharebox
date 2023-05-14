package com.dinhlam.sharebox.modelview.sharelist

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
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.data.model.UserDetail
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
        private val binding: ModelViewShareListTextBinding,
        private val onClick: (String?) -> Unit,
        private val shareToOther: (String) -> Unit,
        private val actionVote: (String) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListTextModelView, ModelViewShareListTextBinding>(
        binding
    ) {

        override fun onBind(model: ShareListTextModelView, position: Int) {
            binding.root.setOnClickListener {
                onClick.invoke(model.content)
            }

            ImageLoader.instance.load(
                buildContext, model.userDetail.avatar, binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }

            binding.layoutBottomAction.buttonUpVote.setOnClickListener {
                actionVote.invoke(model.shareId)
            }

            binding.layoutBottomAction.textUpvote.text =
                buildContext.getString(R.string.up_vote, model.shareUpVote)
            binding.layoutBottomAction.textComment.text =
                buildContext.getString(R.string.comment, model.shareComment)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(buildContext, R.color.colorTextBlack)) {
                    append(model.userDetail.name)
                }
                color(ContextCompat.getColor(buildContext, R.color.colorTextHint)) {
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

    }
}
