package com.dinhlam.sharebox.modelview.sharelist

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ModelViewShareListUrlBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.UserUtils
import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

data class ShareListUrlModelView(
    val shareId: String,
    val iconUrl: String?,
    val url: String?,
    val createdAt: Long,
    val note: String?,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val starred: Boolean = false,
    val userDetail: UserDetail,
    val actionOpen: Function1<String, Unit>? = null,
    val actionShareToOther: Function1<String, Unit>? = null,
    val actionVote: Function1<String, Unit>? = null,
    val actionComment: Function1<String, Unit>? = null,
    val actionStar: Function1<String, Unit>? = null,
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(inflater: LayoutInflater): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListUrlWebHolder(ModelViewShareListUrlBinding.inflate(inflater))
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    private class ShareListUrlWebHolder(
        binding: ModelViewShareListUrlBinding,

        ) : BaseListAdapter.BaseViewHolder<ShareListUrlModelView, ModelViewShareListUrlBinding>(
        binding
    ) {

        override fun onBind(model: ShareListUrlModelView, position: Int) {
            ImageLoader.instance.load(
                buildContext, model.userDetail.avatar, binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.container.setOnClickListener {
                model.actionOpen?.invoke(model.url!!)
            }

            binding.layoutBottomAction.buttonShare.setOnClickListener {
                model.actionShareToOther?.invoke(model.shareId)
            }

            binding.layoutBottomAction.buttonComment.setOnClickListener {
                model.actionComment?.invoke(model.shareId)
            }

            binding.layoutBottomAction.buttonUpVote.setOnClickListener {
                model.actionVote?.invoke(model.shareId)
            }

            binding.layoutBottomAction.buttonStar.setOnClickListener {
                model.actionStar?.invoke(model.shareId)
            }

            if (model.starred) {
                binding.layoutBottomAction.textStarred.setDrawableCompat(start = R.drawable.ic_starred)
            } else {
                binding.layoutBottomAction.textStarred.setDrawableCompat(start = R.drawable.ic_star)
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
                    append(" shares a weblink")
                }
            }
            binding.shareLinkPreview.setLink(model.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 1, maxLineTitle = 1)
            }
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
            binding.shareLinkPreview.resetUi()
            binding.textViewNote.text = null
        }

    }
}
