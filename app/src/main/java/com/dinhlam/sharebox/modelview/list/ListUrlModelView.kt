package com.dinhlam.sharebox.modelview.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.databinding.ModelViewListUrlBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.UserUtils
import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

data class ListUrlModelView(
    val shareId: String,
    val url: String?,
    val shareDate: Long,
    val shareNote: String?,
    val likeNumber: Int = 0,
    val commentNumber: Int = 0,
    val userDetail: UserDetail,
    val bookmarked: Boolean = false,
    val liked: Boolean = false,
    val boxDetail: BoxDetail?,
    val actionOpen: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
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
    val actionStar: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBoxClick: BaseListAdapter.NoHashProp<(BoxDetail?) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListUrlWebHolder(ModelViewListUrlBinding.inflate(inflater, container, false))
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    private class ShareListUrlWebHolder(
        binding: ModelViewListUrlBinding
    ) : BaseListAdapter.BaseViewHolder<ListUrlModelView, ModelViewListUrlBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
        }

        override fun onBind(model: ListUrlModelView, position: Int) {
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.bottomAction.setBookmarkIcon(model.bookmarked.asBookmarkIcon(buildContext))
            binding.bottomAction.setLikeIcon(model.liked.asLikeIcon(buildContext))

            binding.container.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.bottomAction.setOnShareClickListener {
                model.actionShareToOther.prop?.invoke(model.shareId)
            }

            binding.bottomAction.setOnCommentClickListener {
                model.actionComment.prop?.invoke(model.shareId)
            }

            binding.bottomAction.setOnLikeClickListener {
                model.actionLike.prop?.invoke(model.shareId)
            }

            binding.bottomAction.setOnBookmarkClickListener {
                model.actionStar.prop?.invoke(model.shareId)
            }

            binding.bottomAction.setLikeNumber(model.likeNumber)
            binding.bottomAction.setCommentNumber(model.commentNumber)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                bold {
                    append(model.userDetail.name)
                }
                append(buildContext.getString(R.string.share_web_link))
            }
            binding.shareLinkPreview.setLink(model.url) {
                ShareBoxLinkPreviewView.Style(maxLineDesc = 2, maxLineUrl = 1, maxLineTitle = 1)
            }
            binding.layoutUserInfo.textUserLevel.text =
                buildContext.getString(
                    R.string.user_level_format,
                    UserUtils.getLevelTitle(model.userDetail.level),
                    model.shareDate.asElapsedTimeDisplay()
                )

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_community)

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }

            model.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.setReadMoreText(text)
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            binding.shareLinkPreview.release()
            releaseUI()
        }

        private fun releaseUI() {
            binding.textViewNote.text = null
            binding.bottomAction.release()
            ImageLoader.INSTANCE.release(buildContext, binding.layoutUserInfo.imageAvatar)
            binding.layoutUserInfo.textViewName.text = null
            binding.layoutUserInfo.textUserLevel.text = null
        }

    }
}
