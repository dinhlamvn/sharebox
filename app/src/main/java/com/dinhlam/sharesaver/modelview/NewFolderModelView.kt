package com.dinhlam.sharesaver.modelview

import android.view.View
import android.view.View.OnClickListener
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewNewFolderBinding

object NewFolderModelView : BaseListAdapter.BaseModelView("model_view_new_folder") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_new_folder

    override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is NewFolderModelView
    }

    override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
        return other is NewFolderModelView && this == other
    }

    class NewFolderViewHolder(view: View, private val listener: OnClickListener) :
        BaseListAdapter.BaseViewHolder<NewFolderModelView, ModelViewNewFolderBinding>(view) {
        override fun onCreateViewBinding(view: View): ModelViewNewFolderBinding {
            return ModelViewNewFolderBinding.bind(view)
        }

        override fun onBind(item: NewFolderModelView, position: Int) {
            binding.root.setOnClickListener(listener)
        }

        override fun onUnBind() {
        }
    }
}
