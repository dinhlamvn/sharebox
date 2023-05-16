package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ModelViewShareListImagesBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.ImageViewMoreModelView
import com.dinhlam.sharebox.utils.UserUtils

data class ShareListImagesModelView(
    val shareId: String,
    val uris: List<Uri>,
    val createdAt: Long,
    val note: String?,
    val spanCount: Int,
    val modelViews: List<ImageViewMoreModelView>,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userDetail: UserDetail,
    val actionOpen: Function1<String, Unit>? = null,
    val actionShareToOther: Function1<String, Unit>? = null,
    val actionVote: Function1<String, Unit>? = null,
    val actionComment: Function1<String, Unit>? = null,
    val actionStar: Function1<String, Unit>? = null,
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListImagesViewHolder(
            ModelViewShareListImagesBinding.inflate(
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
        binding: ModelViewShareListImagesBinding,
    ) : BaseListAdapter.BaseViewHolder<ShareListImagesModelView, ModelViewShareListImagesBinding>(
        binding
    ) {

        private val adapter = BaseListAdapter.createAdapter {
            addAll(models)
        }

        private val models = mutableListOf<ImageViewMoreModelView>()

        init {
            binding.recyclerViewImage.adapter = adapter
            adapter.requestBuildModelViews()
        }

        override fun onBind(model: ShareListImagesModelView, position: Int) {
            binding.recyclerViewImage.layoutManager =
                GridLayoutManager(buildContext, model.spanCount).apply {
                    spanSizeLookup = BaseSpanSizeLookup(adapter, model.spanCount)
                }

            models.clear()
            models.addAll(model.modelViews)
            adapter.requestBuildModelViews()

            ImageLoader.instance.load(
                buildContext,
                model.userDetail.avatar,
                binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.container.setOnClickListener {
                model.actionOpen?.invoke(model.shareId)
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
