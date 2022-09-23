package com.dinhlam.sharesaver.ui.share.dialog.foldercreator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.base.BaseDialogFragment
import com.dinhlam.sharesaver.databinding.DialogShareFolderCreatorBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareFolderCreatorDialogFragment :
    BaseDialogFragment<ShareFolderCreatorDialogData, ShareFolderCreatorDialogViewModel, DialogShareFolderCreatorBinding>() {

    interface OnShareFolderCreatorCallback {
        fun onFolderCreated(folderId: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogShareFolderCreatorBinding {
        return DialogShareFolderCreatorBinding.inflate(inflater, container, false)
    }

    override val viewModel: ShareFolderCreatorDialogViewModel by viewModels()

    override fun onDataChanged(data: ShareFolderCreatorDialogData) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewModel.consumeOnChange(ShareFolderCreatorDialogData::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textLayoutFolderName.error = getString(errorRes)
            } else {
                viewBinding.textLayoutFolderName.error = null
            }
        }

        viewModel.consumeOnChange(ShareFolderCreatorDialogData::folderIdInserted) { folderId ->
            folderId?.let {
                activity.cast<OnShareFolderCreatorCallback>()?.onFolderCreated(it)
                dismiss()
            }
        }

        viewModel.consumeOnChange(ShareFolderCreatorDialogData::toastRes) { toastRes ->
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