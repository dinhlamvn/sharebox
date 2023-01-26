package com.dinhlam.sharebox.modelview

import android.view.View
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewFolderBinding

data class FolderModelView(
    val id: String,
    val name: String,
    val desc: String?,
    val hasPassword: Boolean = false
) : BaseListAdapter.BaseModelView(id) {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_folder

    class FolderViewHolder(
        view: View,
        private val folderClick: (Int) -> Unit,
        private val folderLongClick: ((View, Int) -> Unit)? = null
    ) : BaseListAdapter.BaseViewHolder<FolderModelView, ModelViewFolderBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewFolderBinding {
            return ModelViewFolderBinding.bind(view)
        }

        override fun onBind(item: FolderModelView, position: Int) {
            binding.root.setOnClickListener {
                folderClick(position)
            }
            folderLongClick?.let {
                binding.root.setOnLongClickListener { clickedView ->
                    it(clickedView, position)
                    return@setOnLongClickListener true
                }
            }

            binding.textViewFolderName.text = item.name
            binding.textViewFolderDesc.text = item.desc
            binding.imageViewKey.isVisible = item.hasPassword
        }

        override fun onUnBind() {
        }
    }
}
