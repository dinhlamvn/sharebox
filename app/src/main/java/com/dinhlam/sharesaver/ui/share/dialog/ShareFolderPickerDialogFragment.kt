package com.dinhlam.sharesaver.ui.share.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseDialogFragment
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.databinding.DialogShareFolderPickerBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.modelview.FolderModelView
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.modelview.NewFolderModelView
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareFolderPickerDialogFragment :
    BaseDialogFragment<ShareFolderPickerDialogData, ShareFolderPickerDialogViewModel, DialogShareFolderPickerBinding>() {

    interface OnShareFolderPickerCallback {
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

    private val folderAdapter = BaseListAdapter.createAdapter { layoutRes, view ->
        return@createAdapter when (layoutRes) {
            R.layout.model_view_folder -> FolderModelView.FolderViewHolder(view) { position ->
                viewModel.onSelectedFolder(position)
            }
            R.layout.model_view_new_folder -> NewFolderModelView.NewFolderViewHolder(view) {
                viewModel.requestCreateNewFolder()
            }
            R.layout.model_view_loading -> LoadingViewHolder(view)
            else -> null
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogShareFolderPickerBinding {
        return DialogShareFolderPickerBinding.inflate(inflater, container, false)
    }

    override val viewModel: ShareFolderPickerDialogViewModel by viewModels()

    override fun onDataChanged(data: ShareFolderPickerDialogData) {
        if (data.selectedFolder != null || data.requestCreateFolder) {
            return
        }
        modelViewsFactory.requestBuildModelViews()
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = BaseSpanSizeLookup(folderAdapter, this)
        }
        viewBinding.recyclerView.adapter = folderAdapter
        modelViewsFactory.attach(folderAdapter)

        viewModel.consumeOnChange(ShareFolderPickerDialogData::selectedFolder) {
            val folderId = it?.id ?: return@consumeOnChange
            activity?.cast<OnShareFolderPickerCallback>()
                ?.onFolderSelected(folderId)?.also {
                    dismiss()
                }
        }

        viewModel.consumeOnChange(ShareFolderPickerDialogData::requestCreateFolder) { isCreateNewFolder ->
            if (isCreateNewFolder) {
                activity?.cast<OnShareFolderPickerCallback>()?.onCreateNewFolder()?.also {
                    dismiss()
                }
            }
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}