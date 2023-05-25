package com.dinhlam.sharebox.ui.home.videomixer.modelview

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewVideoTiktokBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class TiktokVideoModelView(
    val id: String,
    val videoUri: String,
    val shareDetail: ShareDetail,
    val actionOpen: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionShareToOther: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionVote: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionComment: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionStar: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(id) {
    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return TiktokVideoViewHolder(
            ModelViewVideoTiktokBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class TiktokVideoViewHolder(binding: ModelViewVideoTiktokBinding) :
        BaseListAdapter.BaseViewHolder<TiktokVideoModelView, ModelViewVideoTiktokBinding>(binding) {

        private var mediaPlayer: MediaPlayer? = null

        init {
            binding.bottomAction.apply {
                setVoteIcon(R.drawable.ic_arrow_up_white)
                setCommentIcon(R.drawable.ic_comment_white)
                setShareIcon(R.drawable.ic_share_white)
                setVoteTextColor(Color.WHITE)
                setCommentTextColor(Color.WHITE)
            }
            binding.imagePlay.setOnClickListener { view ->
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    binding.imagePlay.setImageResource(R.drawable.ic_play_white)
                } else {
                    mediaPlayer?.start()
                    binding.imagePlay.setImageResource(R.drawable.ic_pause_white)
                }
                view.postDelayed({
                    binding.imagePlay.setImageDrawable(null)
                }, 2000)
            }
            binding.videoView.setOnPreparedListener { mediaPlayer ->
                this.mediaPlayer = mediaPlayer
                mediaPlayer.isLooping = true
                binding.videoView.start()
            }
            binding.videoView.requestFocus()
        }

        override fun onBind(model: TiktokVideoModelView, position: Int) {
            binding.videoView.stopPlayback()
            binding.videoView.setVideoURI(Uri.parse(model.videoUri))

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIcon(
                    R.drawable.ic_bookmarked_white,
                    R.drawable.ic_bookmark_white
                )
            )

            binding.bottomAction.setOnShareClickListener {
                model.actionShareToOther.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnCommentClickListener {
                model.actionComment.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnLikeClickListener {
                model.actionVote.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnBookmarkClickListener {
                model.actionStar.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setVoteNumber(model.shareDetail.voteCount)
            binding.bottomAction.setCommentNumber(model.shareDetail.commentCount)

            binding.textViewName.text = model.shareDetail.user.name
            ImageLoader.instance.load(
                buildContext,
                model.shareDetail.user.avatar,
                binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            model.shareDetail.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textNote.isVisible = true
                binding.textNote.setReadMoreText(text)
            } ?: binding.textNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.videoView.stopPlayback()
            }
            binding.textNote.text = null
            binding.bottomAction.release()
            ImageLoader.instance.release(buildContext, binding.imageAvatar)
        }
    }
}
