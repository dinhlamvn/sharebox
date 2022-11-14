package com.dinhlam.sharesaver.modelview

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewFolderListBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.model.Tag

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
        return other is FolderListModelView && this.id == other.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is FolderListModelView && this == other
    }

    class FolderListViewHolder(
        view: View,
        private val folderClick: (Int) -> Unit,
        private val folderLongClick: ((View, Int) -> Unit)? = null
    ) : BaseListAdapter.BaseViewHolder<FolderListModelView, ModelViewFolderListBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewFolderListBinding {
            return ModelViewFolderListBinding.bind(view)
        }

        override fun onBind(item: FolderListModelView, position: Int) {
            binding.root.setOnClickListener {
                folderClick(position)
            }
            folderLongClick?.let {
                binding.root.setOnLongClickListener { clickedView ->
                    it(clickedView, position)
                    return@setOnLongClickListener true
                }
            }

            binding.textViewFolderUpdatedDate.text = item.updatedAt.format("MMM d H:m")
            binding.textViewFolderName.text = item.name
            binding.textViewFolderDesc.text = item.desc
            binding.imageViewKey.isVisible = item.hasPassword

            val tag = item.tag ?: return binding.cardViewTag.run { isVisible = false }
            binding.cardViewTag.isVisible = true
            binding.cardViewTag.setCardBackgroundColor(tag.color)
        }

        override fun onUnBind() {
        }
    }
}
