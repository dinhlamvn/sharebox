package com.dinhlam.sharebox.modelview.trending

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewTrendingShareImagesBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class TrendingShareImagesModelView(
    val shareId: String,
    val uri: Uri?,
    val numberImage: Int,
    val shareDate: Long,
    val shareNote: String?,
    val likeNumber: Int = 0,
    val commentNumber: Int = 0,
    val userDetail: UserDetail,
    val boxDetail: BoxDetail?,
    val actionOpen: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return TrendingShareWebLinkViewHolder(
            ModelViewTrendingShareImagesBinding.inflate(
                inflater, container, false
            )
        )
    }

    private class TrendingShareWebLinkViewHolder(
        binding: ModelViewTrendingShareImagesBinding
    ) : BaseListAdapter.BaseViewHolder<TrendingShareImagesModelView, ModelViewTrendingShareImagesBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.textLike.setDrawableCompat(start = Icons.likeIcon(buildContext))
            binding.textComment.setDrawableCompat(start = Icons.commentIcon(buildContext))
        }

        override fun onBind(model: TrendingShareImagesModelView, position: Int) {
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.textLike.text = buildContext.getString(R.string.like, model.likeNumber)
            binding.textComment.text = buildContext.getString(R.string.comment, model.commentNumber)

            binding.container.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.textViewName.text = buildSpannedString {
                bold {
                    append(model.userDetail.name)
                }
            }

            ImageLoader.INSTANCE.load(buildContext, model.uri, binding.shareImage) {
                copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
            }

            if (model.numberImage <= 1) {
                binding.textNumber.text = null
            } else {
                binding.textNumber.text =
                    buildContext.getString(R.string.number_image, model.numberImage)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            model.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.text = text
            } ?: binding.textViewNote.apply {
                text = null
                isInvisible = true
            }
        }

        override fun onUnBind() {
            releaseUI()
        }

        private fun releaseUI() {
            binding.textViewNote.text = null
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
            ImageLoader.INSTANCE.release(buildContext, binding.shareImage)
        }
    }
}
