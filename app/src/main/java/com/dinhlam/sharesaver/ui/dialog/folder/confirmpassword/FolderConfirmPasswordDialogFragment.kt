package com.dinhlam.sharesaver.ui.dialog.folder.confirmpassword

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.base.BaseDialogFragment
import com.dinhlam.sharesaver.databinding.DialogFolderConfirmPasswordBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.utils.ExtraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderConfirmPasswordDialogFragment :
    BaseDialogFragment<FolderConfirmPasswordDialogData, FolderConfirmPasswordDialogViewModel, DialogFolderConfirmPasswordBinding>() {

    interface OnConfirmPasswordCallback {
        fun onPasswordVerified()
        fun onCancelConfirmPassword()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderConfirmPasswordBinding {
        return DialogFolderConfirmPasswordBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderConfirmPasswordDialogViewModel by viewModels()

    override fun onDataChanged(data: FolderConfirmPasswordDialogData) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return
        viewModel.loadFolderData(folderId)

        viewModel.consumeOnChange(FolderConfirmPasswordDialogData::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textLayoutFolderPassword.error = getString(errorRes)
            } else {
                viewBinding.textLayoutFolderPassword.error = null
            }
        }

        viewModel.consumeOnChange(FolderConfirmPasswordDialogData::verifyPasswordSuccess) { isPasswordVerified ->
            if (isPasswordVerified) {
                dismiss()
                activity.cast<OnConfirmPasswordCallback>()?.onPasswordVerified()
            }
        }

        viewBinding.textLayoutFolderPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.clearError()
            }
        }

        viewBinding.textInputFolderPassword.doAfterTextChanged {
            viewModel.clearError()
        }

        viewBinding.buttonDone.setOnClickListener {
            val password = viewBinding.textInputFolderPassword.getTrimmedText()
            viewModel.confirmPassword(password)
        }
    }

    override fun getSpacing(): Int {
        return 32
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val isVerified = withData(viewModel) { it.verifyPasswordSuccess }
        if (!isVerified) {
            activity.cast<OnConfirmPasswordCallback>()?.onCancelConfirmPassword()
        }
    }
}