package com.dinhlam.sharesaver.ui.dialog.folder.creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.base.BaseDialogFragment
import com.dinhlam.sharesaver.databinding.DialogFolderCreatorBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderCreatorDialogFragment :
    BaseDialogFragment<FolderCreatorDialogData, FolderCreatorDialogViewModel, DialogFolderCreatorBinding>() {

    interface OnFolderCreatorCallback {
        fun onFolderCreated(folderId: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderCreatorBinding {
        return DialogFolderCreatorBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderCreatorDialogViewModel by viewModels()

    override fun onDataChanged(data: FolderCreatorDialogData) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewModel.consumeOnChange(FolderCreatorDialogData::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textLayoutFolderName.error = getString(errorRes)
            } else {
                viewBinding.textLayoutFolderName.error = null
            }
        }

        viewModel.consumeOnChange(FolderCreatorDialogData::folderIdInserted) { folderId ->
            folderId?.let {
                activity.cast<OnFolderCreatorCallback>()?.onFolderCreated(it)
                dismiss()
            }
        }

        viewModel.consumeOnChange(FolderCreatorDialogData::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewBinding.checkboxPassword.setOnCheckedChangeListener { _, isChecked ->
            viewBinding.textLayoutFolderPassword.isVisible = isChecked
            viewBinding.textLayoutFolderPasswordAlias.isVisible = isChecked
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
        val folderPasswordAlias =
            viewBinding.textInputFolderPasswordAlias.getTrimmedText().takeIfNotNullOrBlank()
        viewModel.createFolder(folderName, folderDesc, folderPassword, folderPasswordAlias)
    }

    override fun getSpacing(): Int {
        return 32
    }
}