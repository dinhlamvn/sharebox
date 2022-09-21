package com.dinhlam.sharesaver.ui.home.modelview

import android.view.View
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewHomeFolderBinding

data class HomeFolderModelView(
    val id: String, val name: String, val desc: String?
) : BaseListAdapter.BaseModelView(id) {
    override val layoutRes: Int
        get() = R.layout.model_view_home_folder

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeFolderModelView && this.id == other.id
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is HomeFolderModelView && this == other
    }

    class HomeFolderViewHolder(view: View, private val folderClick: (Int) -> Unit) :
        BaseListAdapter.BaseViewHolder<HomeFolderModelView, ModelViewHomeFolderBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewHomeFolderBinding {
            return ModelViewHomeFolderBinding.bind(view)
        }

        override fun onBind(item: HomeFolderModelView, position: Int) {
            binding.root.setOnClickListener {
                folderClick(position)
            }
            binding.textViewFolderName.text = item.name
            binding.textViewFolderDesc.text = item.desc
        }

        override fun onUnBind() {

        }
    }
}