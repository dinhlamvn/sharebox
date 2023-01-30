package com.dinhlam.sharebox.dialog.folder.creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogFolderCreatorBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderCreatorDialogFragment :
    BaseViewModelDialogFragment<FolderCreatorDialogState, FolderCreatorDialogViewModel, DialogFolderCreatorBinding>() {

    interface OnFolderCreatorCallback {
        fun onFolderCreated(folderId: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFolderCreatorBinding {
        return DialogFolderCreatorBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderCreatorDialogViewModel by viewModels()

    override fun onStateChanged(state: FolderCreatorDialogState) {
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewModel.consume(viewLifecycleOwner, FolderCreatorDialogState::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textLayoutFolderName.error = getString(errorRes)
            } else {
                viewBinding.textLayoutFolderName.error = null
            }
        }

        viewModel.consume(
            viewLifecycleOwner,
            FolderCreatorDialogState::folderIdInserted
        ) { folderId ->
            folderId?.let {
                getCallback()?.onFolderCreated(it)
                dismiss()
            }
        }

        viewModel.consume(viewLifecycleOwner, FolderCreatorDialogState::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewBinding.checkboxPassword.setOnCheckedChangeListener { _, isChecked ->
            viewBinding.textLayoutFolderPassword.isVisible = isChecked
            viewBinding.textLayoutFolderPasswordReminder.isVisible = isChecked
        }

        viewBinding.textInputFolderName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.clearError()
            }
        }

        viewBinding.textInputFolderName.doAfterTextChanged {
            viewModel.clearError()
        }

        viewBinding.buttonSave.setOnClickListener {
            onCreateFolder()
        }
    }

    private fun onCreateFolder() {
        val folderName = viewBinding.textInputFolderName.getTrimmedText()
        val folderDesc = viewBinding.textInputFolderDesc.getTrimmedText().takeIfNotNullOrBlank()
        val folderPassword =
            viewBinding.textInputFolderPassword.getTrimmedText().takeIfNotNullOrBlank()
        val folderPasswordReminder =
            viewBinding.textInputFolderPasswordReminder.getTrimmedText().takeIfNotNullOrBlank()
        viewModel.createFolder(folderName, folderDesc, folderPassword, folderPasswordReminder)
    }

    override fun getSpacing(): Int {
        return 32
    }

    private fun getCallback(): OnFolderCreatorCallback? {
        return activity?.cast() ?: parentFragment.cast()
    }
}
