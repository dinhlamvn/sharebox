package com.dinhlam.sharebox.modelview.grid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewGridUrlBinding
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class GridUrlModelView(
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
    val actionMore: BaseListAdapter.NoHashProp<(String) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareGridUrlViewHolder(ModelViewGridUrlBinding.inflate(inflater, container, false))
    }

    private class ShareGridUrlViewHolder(
        binding: ModelViewGridUrlBinding
    ) : BaseListAdapter.BaseViewHolder<GridUrlModelView, ModelViewGridUrlBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.textLike.setDrawableCompat(start = Icons.likeIcon(buildContext))
            binding.textComment.setDrawableCompat(start = Icons.commentIcon(buildContext))
            binding.imageMore.setImageDrawable(Icons.moreIcon(buildContext))
        }

        override fun onBind(model: GridUrlModelView, position: Int) {
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.imageMore.setOnClickListener {
                model.actionMore.prop?.invoke(model.shareId)
            }

            binding.textLike.setDrawableCompat(model.liked.asLikeIcon(buildContext))

            binding.textLike.text = buildContext.getString(R.string.like, model.likeNumber)
            binding.textComment.text = buildContext.getString(R.string.comment, model.commentNumber)

            binding.buttonComment.setOnClickListener {
                model.actionComment.prop?.invoke(model.shareId)
            }

            binding.buttonLike.setOnClickListener {
                model.actionLike.prop?.invoke(model.shareId)
            }

            binding.container.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.textViewName.text = buildSpannedString {
                bold {
                    append(model.userDetail.name)
                }
            }
            binding.shareLinkPreview.setLink(model.url) {
                copy(maxLineDesc = 0, maxLineUrl = 0, maxLineTitle = 1)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }

            model.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.text = text
            } ?: binding.textViewNote.apply {
                text = null
                isInvisible = true
            }
        }

        override fun onUnBind() {
            binding.shareLinkPreview.release()
            releaseUI()
        }

        private fun releaseUI() {
            binding.textViewNote.text = null
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }

    }
}
