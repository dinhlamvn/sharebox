package com.dinhlam.sharebox.modelview.list.video

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewListVideoTiktokBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.IconUtils
import com.dinhlam.sharebox.utils.UserUtils

data class ListTiktokVideoModelView(
    val id: String,
    val videoUri: String,
    val shareDetail: ShareDetail,
    val actionViewInSource: BaseListAdapter.NoHashProp<Function1<ShareData, Unit>> = BaseListAdapter.NoHashProp(
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
    val actionSaveToGallery: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(id) {
    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ListTiktokVideoViewHolder(
            ModelViewListVideoTiktokBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class ListTiktokVideoViewHolder(binding: ModelViewListVideoTiktokBinding) :
        BaseListAdapter.BaseViewHolder<ListTiktokVideoModelView, ModelViewListVideoTiktokBinding>(
            binding
        ) {

        private var mediaPlayer: MediaPlayer? = null

        init {
            binding.textBoxName.setDrawableCompat(start = IconUtils.boxIcon(buildContext) {
                copy(sizeDp = 12)
            })
            binding.videoView.setOnPreparedListener { mediaPlayer ->
                this.mediaPlayer = mediaPlayer
                mediaPlayer.isLooping = true
                binding.videoView.start()
            }
            binding.videoView.requestFocus()
        }

        override fun onBind(model: ListTiktokVideoModelView, position: Int) {
            binding.videoView.stopPlayback()
            binding.videoView.setVideoURI(Uri.parse(model.videoUri))

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIcon(buildContext)
            )

            binding.bottomAction.setLikeIcon(model.shareDetail.liked.asLikeIcon(buildContext))

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

            ImageLoader.INSTANCE.load(
                buildContext,
                model.shareDetail.user.avatar,
                binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                bold {
                    append(model.shareDetail.user.name)
                }
                append(buildContext.getString(R.string.share_image))
            }
            binding.layoutUserInfo.textUserLevel.text =
                buildContext.getString(
                    R.string.user_level_format,
                    UserUtils.getLevelTitle(model.shareDetail.user.level),
                    model.shareDetail.shareDate.asElapsedTimeDisplay()
                )

            binding.textBoxName.text =
                model.shareDetail.boxDetail?.boxName ?: buildContext.getText(R.string.box_community)

            model.shareDetail.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.setReadMoreText(text)
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.videoView.stopPlayback()
            }
            binding.textViewNote.text = null
            binding.bottomAction.release()
            ImageLoader.INSTANCE.release(buildContext, binding.layoutUserInfo.imageAvatar)
        }
    }
}
