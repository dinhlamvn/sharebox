package com.dinhlam.sharebox.dialog.folder.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.databinding.DialogFolderDetailBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.utils.ExtraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderDetailDialogFragment :
    BaseViewModelDialogFragment<FolderDetailDialogState, FolderDetailDialogViewModel, DialogFolderDetailBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFolderDetailBinding {
        return DialogFolderDetailBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderDetailDialogViewModel by viewModels()

    override fun onStateChanged(state: FolderDetailDialogState) {
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return dismiss()
        viewModel.loadFolderData(folderId)
        viewModel.consume(viewLifecycleOwner, FolderDetailDialogState::folder, true, ::onRenderFolderDetail)
        viewModel.consume(viewLifecycleOwner, FolderDetailDialogState::shareCount, false, ::onShareCountChange)

        viewBinding.buttonClose.setOnClickListener { dismiss() }
    }

    private fun onShareCountChange(shareCount: Int) {
        viewBinding.textFolderShareCount.text =
            resources.getQuantityString(R.plurals.share_count_text, shareCount, shareCount)
    }

    private fun onRenderFolderDetail(folder: Folder?) {
        val nonNullFolder = folder ?: return
        viewBinding.textFolderName.text = nonNullFolder.name
        viewBinding.textFolderDesc.text =
            nonNullFolder.desc.takeIfNotNullOrBlank() ?: getString(R.string.folder_desc_empty)
        viewBinding.textFolderCreated.text =
            nonNullFolder.createdAt.format("E, d MMMM yyyy, h:mm:ss a")
        viewBinding.textFolderUpdated.text =
            nonNullFolder.updatedAt.format("E, d MMMM yyyy, h:mm:ss a")
        viewBinding.textFolderHasPassword.text = if (!nonNullFolder.password.isNullOrBlank()) {
            "Yes"
        } else {
            "No"
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}
