package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ModelViewShareListImageBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.UserUtils

data class ShareListImageModelView(
    val shareId: String,
    val uri: Uri,
    val createdAt: Long,
    val note: String?,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userDetail: UserDetail,
    val actionOpen: Function1<Uri, Unit>? = null,
    val actionShareToOther: Function1<String, Unit>? = null,
    val actionVote: Function1<String, Unit>? = null,
    val actionComment: Function1<String, Unit>? = null,
    val actionStar: Function1<String, Unit>? = null,
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(inflater: LayoutInflater): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListImageViewHolder(ModelViewShareListImageBinding.inflate(inflater))
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    private class ShareListImageViewHolder(
        binding: ModelViewShareListImageBinding,
    ) : BaseListAdapter.BaseViewHolder<ShareListImageModelView, ModelViewShareListImageBinding>(
        binding
    ) {

        override fun onBind(model: ShareListImageModelView, position: Int) {
            ImageLoader.instance.load(
                buildContext,
                model.userDetail.avatar,
                binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.container.setOnClickListener {
                model.actionOpen?.invoke(model.uri)
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

            binding.layoutBottomAction.textUpvote.text =
                buildContext.getString(R.string.up_vote, model.shareUpVote)
            binding.layoutBottomAction.textComment.text =
                buildContext.getString(R.string.comment, model.shareComment)
            ImageLoader.instance.load(buildContext, model.uri, binding.imageShare)
            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(buildContext, R.color.colorTextBlack)) {
                    append(model.userDetail.name)
                }
                color(ContextCompat.getColor(buildContext, R.color.colorTextHint)) {
                    append(" shares an image")
                }
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
        }

    }
}
