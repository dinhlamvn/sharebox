package com.dinhlam.sharesaver.ui.dialog.folder.selector

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
    BaseViewModelDialogFragment<FolderSelectorDialogData, FolderSelectorDialogViewModel, DialogFolderSelectorBinding>() {

    interface OnFolderSelectorCallback {
        fun onFolderSelected(folderId: String)
        fun onCreateNewFolder()
    }

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {
        override fun buildModelViews() = withData(viewModel) { data ->
            if (data.isFirstLoad) {
                return@withData LoadingModelView.addTo(this)
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

    override fun onDataChanged(data: FolderSelectorDialogData) {
        if (data.selectedFolder != null || data.requestCreateFolder) {
            return
        }
        modelViewsFactory.requestBuildModelViews()
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = BaseSpanSizeLookup(folderAdapter, this)
        }
        viewBinding.recyclerView.setupWith(folderAdapter, modelViewsFactory)

        viewModel.consumeOnChange(viewLifecycleOwner, FolderSelectorDialogData::selectedFolder) {
            val folderId = it?.id ?: return@consumeOnChange
            getCallback()?.onFolderSelected(folderId)?.also {
                dismiss()
            }
        }

        viewModel.consumeOnChange(
            viewLifecycleOwner,
            FolderSelectorDialogData::requestCreateFolder
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