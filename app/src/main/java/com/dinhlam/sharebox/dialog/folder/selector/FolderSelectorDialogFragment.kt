package com.dinhlam.sharebox.dialog.folder.selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogFolderSelectorBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.modelview.FolderModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.NewFolderModelView
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderSelectorDialogFragment :
    BaseViewModelDialogFragment<FolderSelectorDialogState, FolderSelectorDialogViewModel, DialogFolderSelectorBinding>() {

    interface OnFolderSelectorCallback {
        fun onFolderSelected(folderId: String)
        fun onCreateNewFolder()
    }

    private val folderAdapter = BaseListAdapter.createAdapter({
        mutableListOf<BaseListAdapter.BaseModelView>().apply {
            getState(viewModel) { state ->
                if (state.isFirstLoad) {
                    return@getState add(0, LoadingModelView)
                }
                state.folders.forEach {
                    add(FolderModelView("folder_${it.id}", it.name, it.desc))
                }
                add(NewFolderModelView)
            }
        }
    }) {
        withViewType(R.layout.model_view_folder) {
            FolderModelView.FolderViewHolder(this, viewModel::onSelectedFolder)
        }

        withViewType(R.layout.model_view_new_folder) {
            NewFolderModelView.NewFolderViewHolder(this) {
                viewModel.requestCreateNewFolder()
            }
        }

        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderSelectorBinding {
        return DialogFolderSelectorBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderSelectorDialogViewModel by viewModels()

    override fun onStateChanged(state: FolderSelectorDialogState) {
        if (state.selectedFolder != null || state.requestCreateFolder) {
            return
        }
        folderAdapter.requestBuildModelViews()
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = BaseSpanSizeLookup(folderAdapter, this)
        }
        viewBinding.recyclerView.adapter = folderAdapter

        viewModel.consume(viewLifecycleOwner, FolderSelectorDialogState::selectedFolder) {
            val folderId = it?.id ?: return@consume
            getCallback()?.onFolderSelected(folderId)?.also {
                dismiss()
            }
        }

        viewModel.consume(
            viewLifecycleOwner, FolderSelectorDialogState::requestCreateFolder
        ) { isCreateNewFolder ->
            if (isCreateNewFolder) {
                getCallback()?.onCreateNewFolder()?.also {
                    dismiss()
                }
            }
        }
    }

    override fun getSpacing(): Int {
        return 32
    }

    private fun getCallback(): OnFolderSelectorCallback? {
        return activity?.cast() ?: parentFragment.cast()
    }
}
