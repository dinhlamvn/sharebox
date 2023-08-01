package com.dinhlam.sharebox.modelview.videomixer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewVideoBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.UserUtils
import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

data class VideoModelView(
    val id: String,
    val entityId: Int,
    val videoSource: VideoSource,
    val videoUri: String,
    val shareDetail: ShareDetail,
    val actionViewInSource: BaseListAdapter.NoHashProp<Function2<VideoSource, ShareData, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionShareToOther: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionLike: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionComment: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBookmark: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionSaveToGallery: BaseListAdapter.NoHashProp<Function3<Int, VideoSource, String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBoxClick: BaseListAdapter.NoHashProp<(BoxDetail?) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(id) {
    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return TiktokVideoViewHolder(
            ModelViewVideoBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class TiktokVideoViewHolder(binding: ModelViewVideoBinding) :
        BaseListAdapter.BaseViewHolder<VideoModelView, ModelViewVideoBinding>(binding) {

        init {
            binding.imageSaveToGallery.setImageDrawable(Icons.saveIcon(buildContext))
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 12)
            })
            binding.textViewInSource.setDrawableCompat(end = Icons.openIcon(buildContext) {
                copy(sizeDp = 16)
            })
        }

        override fun onBind(model: VideoModelView, position: Int) {
            binding.textViewInSource.text = model.videoSource.sourceName

            binding.textViewInSource.setOnClickListener {
                model.actionViewInSource.prop?.invoke(
                    model.videoSource,
                    model.shareDetail.shareData
                )
            }

            binding.imageSaveToGallery.setOnClickListener {
                model.actionSaveToGallery.prop?.invoke(
                    model.entityId,
                    model.videoSource,
                    model.videoUri
                )
            }

            ImageLoader.INSTANCE.load(
                buildContext, model.shareDetail.user.avatar, binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIcon(
                    buildContext
                )
            )
            binding.bottomAction.setLikeIcon(model.shareDetail.liked.asLikeIcon(buildContext))

            binding.container.setOnClickListener {
                model.actionShareToOther.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnShareClickListener {
                model.actionShareToOther.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnCommentClickListener {
                model.actionComment.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnLikeClickListener {
                model.actionLike.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnBookmarkClickListener {
                model.actionBookmark.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setLikeNumber(model.shareDetail.likeNumber)
            binding.bottomAction.setCommentNumber(model.shareDetail.commentNumber)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                bold {
                    append(model.shareDetail.user.name)
                }
                append(buildContext.getString(R.string.share_video))
            }
            binding.shareLinkPreview.setLink(model.videoUri) {
                ShareBoxLinkPreviewView.Style(
                    maxLineDesc = 0,
                    maxLineUrl = 0,
                    maxLineTitle = 1,
                    imageHeight = 100.dp()
                )
            }
            binding.layoutUserInfo.textUserLevel.text =
                buildContext.getString(
                    R.string.user_level_format,
                    UserUtils.getLevelTitle(model.shareDetail.user.level),
                    model.shareDetail.shareDate.asElapsedTimeDisplay()
                )

            binding.textBoxName.text =
                model.shareDetail.boxDetail?.boxName ?: buildContext.getText(R.string.box_community)

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.shareDetail.boxDetail)
            }

            model.shareDetail.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.setReadMoreText(text)
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            binding.textBoxName.text = null
            binding.textViewNote.text = null
            binding.textViewInSource.text = null
            binding.bottomAction.release()
            ImageLoader.INSTANCE.release(buildContext, binding.layoutUserInfo.imageAvatar)
        }
    }
}
