package com.dinhlam.sharebox.ui.home.videomixer.modelview

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewVideoTiktokBinding
import com.dinhlam.sharebox.extensions.asBookmarkIconLight
import com.dinhlam.sharebox.extensions.asLikeIconLight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.IconUtils

data class TiktokVideoModelView(
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
            binding.imageCollapse.setImageDrawable(
                IconUtils.expandLessIconLight(
                    buildContext
                )
            )
            binding.root.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {

                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {

                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (currentId == R.id.start) {
                        binding.imageCollapse.setImageDrawable(
                            IconUtils.expandLessIconLight(
                                buildContext
                            )
                        )
                    } else {
                        binding.imageCollapse.setImageDrawable(
                            IconUtils.expandMoreIconLight(
                                buildContext
                            )
                        )
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {

                }
            })
            binding.imageSaveToGallery.setImageDrawable(IconUtils.saveIconLight(buildContext))
            binding.textBoxName.setDrawableCompat(start = IconUtils.boxIcon(buildContext) {
                copy(sizeDp = 12, colorRes = android.R.color.white)
            })
            binding.textViewInSource.setDrawableCompat(end = IconUtils.openIcon(buildContext) {
                copy(sizeDp = 16, colorRes = android.R.color.white)
            })
            binding.bottomAction.apply {
                setCommentIcon(IconUtils.commentIconLight(buildContext))
                setShareIcon(IconUtils.shareIconLight(buildContext))
                setLikeTextColor(Color.WHITE)
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
                model.shareDetail.bookmarked.asBookmarkIconLight(buildContext)
            )

            binding.textViewInSource.setOnClickListener {
                model.actionViewInSource.prop?.invoke(model.shareDetail.shareData)
            }

            binding.bottomAction.setLikeIcon(model.shareDetail.liked.asLikeIconLight(buildContext))

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

            binding.imageSaveToGallery.setOnClickListener {
                model.actionSaveToGallery.prop?.invoke(model.videoUri)
            }

            binding.bottomAction.setLikeNumber(model.shareDetail.likeNumber)
            binding.bottomAction.setCommentNumber(model.shareDetail.commentNumber)

            binding.textViewName.text = model.shareDetail.user.name
            ImageLoader.INSTANCE.load(
                buildContext,
                model.shareDetail.user.avatar,
                binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.textBoxName.text =
                model.shareDetail.boxDetail?.boxName ?: buildContext.getText(R.string.box_community)

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
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }
    }
}
