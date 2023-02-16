package com.dinhlam.sharebox.modelview

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewFolderListBinding
import com.dinhlam.sharebox.extensions.asDisplayCountValue
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.model.Tag

data class FolderListModelView(
    val id: String,
    val name: String,
    val desc: String?,
    val updatedAt: Long,
    val hasPassword: Boolean = false,
    val tag: Tag? = null,
    val shareCount: Int = 0
) : BaseListAdapter.BaseModelView(id) {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_folder_list

    class FolderListViewHolder(
        view: View,
        private val folderClick: (Int) -> Unit,
        private val folderOptionClick: ((Int) -> Unit)? = null
    ) : BaseListAdapter.BaseViewHolder<FolderListModelView, ModelViewFolderListBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewFolderListBinding {
            return ModelViewFolderListBinding.bind(view)
        }

        override fun onBind(model: FolderListModelView, position: Int) {
            binding.root.setOnClickListener {
                folderClick(position)
            }

            binding.imageViewOption.setOnClickListener {
                folderOptionClick?.invoke(position)
            }

            binding.textViewShareCount.text = model.shareCount.asDisplayCountValue()

            binding.textViewFolderName.text = model.name
            binding.textViewFolderDesc.text = model.desc
            binding.imageViewKey.isVisible = model.hasPassword
            binding.textViewFolderUpdated.text = model.updatedAt.format("MMM d h:mm a")

            val tag = model.tag ?: return binding.imageViewTag.run { isVisible = false }
            binding.imageViewTag.isVisible = true
            binding.imageViewTag.setImageResource(tag.tagResource)
        }

        override fun onUnBind() {
        }
    }
}
