package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelLibraryEmptyBinding

data class LibraryEmptyListModel(val onAdd: BaseListAdapter.NoHashProp<View.OnClickListener>) :
    BaseListAdapter.BaseListModel("library_empty") {
    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*> =
        object : BaseListAdapter.BaseViewHolderViewBinding<LibraryEmptyListModel, ListModelLibraryEmptyBinding>(
            ListModelLibraryEmptyBinding.inflate(inflater, container, false)) {
            override fun onBind(model: LibraryEmptyListModel, position: Int) {
                binding.buttonAdd.setOnClickListener(model.onAdd.prop)
            }
        }
}
