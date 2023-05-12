package com.dinhlam.sharebox.modelview.sharelist

import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareListImagesBinding
import com.dinhlam.sharebox.extensions.formatForFeed
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveImagesModelView
import com.dinhlam.sharebox.utils.UserUtils

data class ShareListImagesModelView(
    val id: String,
    val uris: List<Uri>,
    val createdAt: Long,
    val note: String?,
    val shareId: Int,
    val spanCount: Int,
    val modelViews: List<ShareReceiveImagesModelView>,
    val shareUpVote: Int = 0,
    val shareComment: Int = 0,
    val userDetail: UserDetail
) : BaseListAdapter.BaseModelView(id) {

    override val modelLayoutRes: Int
        get() = R.layout.model_view_share_list_images

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ShareListMultipleImageViewHolder(
        view: View, private val shareToOther: (Int) -> Unit, private val viewImage: (Uri) -> Unit
    ) : BaseListAdapter.BaseViewHolder<ShareListImagesModelView, ModelViewShareListImagesBinding>(
        view
    ) {

        private val adapter = BaseListAdapter.createAdapter({
            addAll(models)
        }) {
            withViewType(R.layout.model_view_share_receive_images) {
                ShareReceiveImagesModelView.ShareImagesViewHolder(this)
            }
        }

        private val models = mutableListOf<ShareReceiveImagesModelView>()

        init {
            binding.recyclerViewImage.adapter = adapter
            adapter.requestBuildModelViews()
        }

        override fun onBind(model: ShareListImagesModelView, position: Int) {
            binding.root.setOnClickListener {

            }

            binding.recyclerViewImage.layoutManager =
                GridLayoutManager(context, model.spanCount).apply {
                    spanSizeLookup = BaseSpanSizeLookup(adapter, model.spanCount)
                }

            models.clear()
            models.addAll(model.modelViews)
            adapter.requestBuildModelViews()

            ImageLoader.load(
                context,
                model.userDetail.avatar,
                binding.layoutUserInfo.imageAvatar,
                R.drawable.no_preview_image,
                true
            )
            binding.layoutBottomAction.buttonShare.setOnClickListener {
                shareToOther(model.shareId)
            }

            binding.layoutBottomAction.textUpvote.text =
                context.getString(R.string.up_vote, model.shareUpVote)
            binding.layoutBottomAction.textComment.text =
                context.getString(R.string.comment, model.shareComment)

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                color(ContextCompat.getColor(context, R.color.colorTextBlack)) {
                    append(model.userDetail.name)
                }
                color(ContextCompat.getColor(context, R.color.colorTextHint)) {
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

        override fun onCreateViewBinding(view: View): ModelViewShareListImagesBinding {
            return ModelViewShareListImagesBinding.bind(view)
        }
    }
}
