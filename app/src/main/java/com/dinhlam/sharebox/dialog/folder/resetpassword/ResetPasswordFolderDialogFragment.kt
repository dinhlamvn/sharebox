package com.dinhlam.sharebox.dialog.folder.resetpassword

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogFolderResetPasswordBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.utils.ExtraUtils
import com.dinhlam.sharebox.utils.KeyboardUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFolderDialogFragment :
    BaseViewModelDialogFragment<ResetPasswordFolderDialogState, ResetPasswordFolderDialogViewModel, DialogFolderResetPasswordBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderResetPasswordBinding {
        return DialogFolderResetPasswordBinding.inflate(inflater, container, false)
    }

    override val viewModel: ResetPasswordFolderDialogViewModel by viewModels()

    override fun onStateChanged(state: ResetPasswordFolderDialogState) {
        when (val error = state.error) {
            ResetPasswordFolderDialogState.Error.RequireInputPasswordRecovery -> {
                viewBinding.textLayoutPasswordRecoveryHash.error = getString(error.errorMessageRes)
            }
            ResetPasswordFolderDialogState.Error.RequireInputPassword -> {
                viewBinding.textLayoutNewPassword.error = getString(error.errorMessageRes)
            }
            ResetPasswordFolderDialogState.Error.RequireInputPasswordConfirm -> {
                viewBinding.textLayoutNewPasswordConfirm.error = getString(error.errorMessageRes)
            }
            ResetPasswordFolderDialogState.Error.PasswordRecoveryIncorrect -> {
                viewBinding.textLayoutPasswordRecoveryHash.error = getString(error.errorMessageRes)
            }
            ResetPasswordFolderDialogState.Error.ConfirmPasswordNotSame -> {
                viewBinding.textLayoutNewPasswordConfirm.error = getString(error.errorMessageRes)
            }
            ResetPasswordFolderDialogState.Error.UnknownError -> {
                showToast(error.errorMessageRes)
            }
            else -> {
                viewBinding.textLayoutPasswordRecoveryHash.error = null
                viewBinding.textLayoutNewPassword.error = null
                viewBinding.textLayoutNewPasswordConfirm.error = null
            }
        }
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return dismiss()
        viewModel.loadFolderData(folderId)

        viewModel.consume(
            viewLifecycleOwner, ResetPasswordFolderDialogState::changePasswordSuccess
        ) { isChangePasswordSuccess ->
            if (isChangePasswordSuccess) {
                dismiss()
            }
        }

        val focusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.clearError()
            }
        }

        viewBinding.textInputPasswordRecoveryHash.onFocusChangeListener = focusListener
        viewBinding.textInputNewPassword.onFocusChangeListener = focusListener
        viewBinding.textInputNewPasswordConfirm.onFocusChangeListener = focusListener

        val textChangeWatcher: (Editable?) -> Unit = {
            viewModel.clearError()
        }

        viewBinding.textInputPasswordRecoveryHash.doAfterTextChanged(textChangeWatcher)
        viewBinding.textInputNewPassword.doAfterTextChanged(textChangeWatcher)
        viewBinding.textInputNewPasswordConfirm.doAfterTextChanged(textChangeWatcher)

        viewBinding.buttonDone.setOnClickListener {
            viewModel.clearError()
            activity?.currentFocus?.let {
                KeyboardUtil.hideKeyboard(it)
            }
            val passwordRecoveryHash = viewBinding.textInputPasswordRecoveryHash.getTrimmedText()
            val newPassword = viewBinding.textInputNewPassword.getTrimmedText()
            val newPasswordConfirm = viewBinding.textInputNewPasswordConfirm.getTrimmedText()
            viewModel.resetFolderPassword(passwordRecoveryHash, newPassword, newPasswordConfirm)
        }

        viewBinding.imageViewPaste.setOnClickListener {
            viewBinding.textInputPasswordRecoveryHash.setText(
                KeyboardUtil.getTextFromClipboard(requireContext())
            )
        }
    }

    override fun getSpacing(): Int {
        return 32
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val isVerified = getState(viewModel) { it.changePasswordSuccess }
        if (isVerified) {
            showToast(R.string.change_folder_password_success)
        }
    }
}
