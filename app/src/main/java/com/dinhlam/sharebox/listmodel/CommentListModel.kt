package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewCommentBinding
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class CommentListModel(
    val id: Int,
    val name: String,
    val avatar: String,
    val content: String?,
    val commentDate: Long,
) : BaseListAdapter.BaseListModel("comment_$id") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return CommentViewHolder(ModelViewCommentBinding.inflate(inflater, container, false))
    }

    private class CommentViewHolder(binding: ModelViewCommentBinding) :
        BaseListAdapter.BaseViewHolder<CommentListModel, ModelViewCommentBinding>(binding) {
        override fun onBind(model: CommentListModel, position: Int) {
            binding.textName.text = buildSpannedString {
                bold {
                    append(model.name)
                }
                append(" • ")
                append(model.commentDate.asElapsedTimeDisplay())
            }
            ImageLoader.INSTANCE.load(buildContext, model.avatar, binding.imageAvatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            binding.textContent.setReadMoreText(model.content)
        }

        override fun onUnBind() {

        }
    }
}
