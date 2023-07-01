package com.dinhlam.sharebox.modelview.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ModelViewListImagesBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.ImageModelView
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalCirclePagerItemDecoration
import com.dinhlam.sharebox.utils.UserUtils

data class ListImagesModelView(
    val shareId: String,
    val uris: List<Uri>,
    val shareDate: Long,
    val shareNote: String?,
    val modelViews: List<ImageModelView>,
    val likeNumber: Int = 0,
    val commentNumber: Int = 0,
    val userDetail: UserDetail,
    val bookmarked: Boolean = false,
    val liked: Boolean = false,
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
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListImagesViewHolder(
            ModelViewListImagesBinding.inflate(
                inflater,
                container,
                false
            )
        )
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    private class ShareListImagesViewHolder(
        binding: ModelViewListImagesBinding,
    ) : BaseListAdapter.BaseViewHolder<ListImagesModelView, ModelViewListImagesBinding>(
        binding
    ) {

        private val adapter = BaseListAdapter.createAdapter {
            addAll(models)
        }

        private val models = mutableListOf<ImageModelView>()

        init {
            binding.recyclerViewImage.updateLayoutParams {
                height = buildContext.screenHeight().times(0.5f).toInt()
            }
            PagerSnapHelper().attachToRecyclerView(binding.recyclerViewImage)
            binding.recyclerViewImage.addItemDecoration(
                HorizontalCirclePagerItemDecoration(
                    colorActive = ContextCompat.getColor(buildContext, R.color.colorPrimaryDark)
                )
            )
            binding.recyclerViewImage.adapter = adapter
            adapter.requestBuildModelViews()
        }

        override fun onBind(model: ListImagesModelView, position: Int) {
            models.clear()
            models.addAll(model.modelViews)
            adapter.requestBuildModelViews()

            ImageLoader.INSTANCE.load(
                buildContext,
                model.userDetail.avatar,
                binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.bottomAction.setBookmarkIcon(model.bookmarked.asBookmarkIcon(buildContext))
            binding.bottomAction.setLikeIcon(model.liked.asLikeIcon(buildContext))

            model.actionOpen.prop?.let { prop ->
                binding.container.setOnClickListener {
                    prop.invoke(model.shareId)
                }
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
                append(buildContext.getString(R.string.share_images))
            }
            binding.layoutUserInfo.textUserLevel.text =
                buildContext.getString(
                    R.string.user_level_format,
                    UserUtils.getLevelTitle(model.userDetail.level),
                    model.shareDate.asElapsedTimeDisplay()
                )

            model.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.setReadMoreText(text)
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            models.clear()
            adapter.requestBuildModelViews()
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
