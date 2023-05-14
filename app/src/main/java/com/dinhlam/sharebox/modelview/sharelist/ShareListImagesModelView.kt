package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListImagesBinding
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImagesBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveImagesModelView
import com.dinhlam.sharebox.utils.UserUtils

data class ShareListImagesModelView(
    val shareId: String,
    val uris: List<Uri>,
    val createdAt: Long,
    val note: String?,
    val spanCount: Int,
    val modelViews: List<ShareReceiveImagesModelView>,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userDetail: UserDetail
) : BaseListAdapter.BaseModelView(shareId) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_images

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListImagesViewHolder(
        private val binding: ModelViewShareListImagesBinding,
        private val shareToOther: (String) -> Unit,
        private val viewImages: (List<Uri>) -> Unit,
        private val actionVote: (String) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListImagesModelView, ModelViewShareListImagesBinding>(
        binding
    ) {

        private val adapter = BaseListAdapter.createAdapter({
            addAll(models)
        }) {
            withViewType(R.layout.model_view_share_receive_images) {
                ShareReceiveImagesModelView.ShareReceiveImagesViewHolder(
                    ModelViewShareReceiveImagesBinding.bind(this)
                ) {
                    viewImages.invoke(models.map { it.uri })
                }
            }
        }

        private val models = mutableListOf<ShareReceiveImagesModelView>()

        init {
            binding.recyclerViewImage.adapter = adapter
            adapter.requestBuildModelViews()
        }

        override fun onBind(model: ShareListImagesModelView, position: Int) {
            binding.root.setOnClickListener {
                viewImages(model.uris)
            }

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

            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }

            binding.layoutBottomAction.buttonUpVote.setOnClickListener {
                actionVote.invoke(model.shareId)
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
