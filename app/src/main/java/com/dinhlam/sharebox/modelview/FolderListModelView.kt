package com.dinhlam.sharebox.modelview

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewFolderListBinding
import com.dinhlam.sharebox.model.Tag

data class FolderListModelView(
    val id: String,
    val name: String,
    val desc: String?,
    val updatedAt: Long,
    val hasPassword: Boolean = false,
    val tag: Tag? = null
) : BaseListAdapter.BaseModelView(id) {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_folder_list

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return this.modelId == other.modelId
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return this === other
    }

    class FolderListViewHolder(
        view: View,
        private val folderClick: (Int) -> Unit,
        private val folderOptionClick: ((View, Int) -> Unit)? = null
    ) : BaseListAdapter.BaseViewHolder<FolderListModelView, ModelViewFolderListBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewFolderListBinding {
            return ModelViewFolderListBinding.bind(view)
        }

        override fun onBind(item: FolderListModelView, position: Int) {
            binding.root.setOnClickListener {
                folderClick(position)
            }

            binding.imageViewOption.setOnClickListener { view ->
                folderOptionClick?.invoke(view, position)
            }

            binding.textViewFolderName.text = item.name
            binding.textViewFolderDesc.text = item.desc
            binding.imageViewKey.isVisible = item.hasPassword

            val tag = item.tag ?: return binding.imageViewTag.run { isVisible = false }
            binding.imageViewTag.isVisible = true
            binding.imageViewTag.setImageResource(tag.tagResource)
        }

        override fun onUnBind() {
        }
    }
}
