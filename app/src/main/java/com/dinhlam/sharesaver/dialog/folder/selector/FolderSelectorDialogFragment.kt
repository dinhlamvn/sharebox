package com.dinhlam.sharesaver.dialog.folder.selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.base.BaseViewModelDialogFragment
import com.dinhlam.sharesaver.databinding.DialogFolderSelectorBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.setupWith
import com.dinhlam.sharesaver.modelview.FolderModelView
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.modelview.NewFolderModelView
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderSelectorDialogFragment :
    BaseViewModelDialogFragment<FolderSelectorDialogState, FolderSelectorDialogViewModel, DialogFolderSelectorBinding>() {

    interface OnFolderSelectorCallback {
        fun onFolderSelected(folderId: String)
        fun onCreateNewFolder()
    }

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {
        override fun buildModelViews() = withState(viewModel) { data ->
            if (data.isFirstLoad) {
                return@withState LoadingModelView.addTo(this)
            }
            data.folders.forEach {
                FolderModelView("folder_${it.id}", it.name, it.desc).addTo(this)
            }
            NewFolderModelView.addTo(this)
        }
    }

    private val folderAdapter = BaseListAdapter.createAdapter {
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
        modelViewsFactory.requestBuildModelViews()
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = BaseSpanSizeLookup(folderAdapter, this)
        }
        viewBinding.recyclerView.setupWith(folderAdapter, modelViewsFactory)

        viewModel.consume(viewLifecycleOwner, FolderSelectorDialogState::selectedFolder) {
            val folderId = it?.id ?: return@consume
            getCallback()?.onFolderSelected(folderId)?.also {
                dismiss()
            }
        }

        viewModel.consume(
            viewLifecycleOwner,
            FolderSelectorDialogState::requestCreateFolder
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