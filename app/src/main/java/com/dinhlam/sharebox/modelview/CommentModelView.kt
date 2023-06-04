package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewCommentBinding
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class CommentModelView(
    val id: Int,
    val name: String,
    val avatar: String,
    val content: String?,
    val commentDate: Long,
) : BaseListAdapter.BaseModelView("comment_$id") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return CommentViewHolder(ModelViewCommentBinding.inflate(inflater, container, false))
    }

    private class CommentViewHolder(binding: ModelViewCommentBinding) :
        BaseListAdapter.BaseViewHolder<CommentModelView, ModelViewCommentBinding>(binding) {
        override fun onBind(model: CommentModelView, position: Int) {
            binding.textName.text = buildSpannedString {
                color(ContextCompat.getColor(buildContext, R.color.colorTextBlack)) {
                    append(model.name)
                }
                color(ContextCompat.getColor(buildContext, R.color.colorHint)) {
                    append(" • ")
                    append(model.commentDate.asElapsedTimeDisplay())
                }
            }
            ImageLoader.instance.load(buildContext, model.avatar, binding.imageAvatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            binding.textContent.setReadMoreText(model.content)
        }

        override fun onUnBind() {

        }
    }
}
