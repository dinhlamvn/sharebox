package com.dinhlam.sharesaver.ui.dialog.folder.confirmpassword

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModelDialogFragment
import com.dinhlam.sharesaver.databinding.DialogFolderConfirmPasswordBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.utils.ExtraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderConfirmPasswordDialogFragment :
    BaseViewModelDialogFragment<FolderConfirmPasswordDialogState, FolderConfirmPasswordDialogViewModel, DialogFolderConfirmPasswordBinding>() {

    interface OnConfirmPasswordCallback {
        fun onPasswordVerified(isRemindPassword: Boolean = false)
        fun onCancelConfirmPassword()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderConfirmPasswordBinding {
        return DialogFolderConfirmPasswordBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderConfirmPasswordDialogViewModel by viewModels()

    override fun onDataChanged(data: FolderConfirmPasswordDialogState) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return dismiss()
        viewModel.loadFolderData(folderId)

        viewModel.consume(viewLifecycleOwner, FolderConfirmPasswordDialogState::folder) { folder ->
            val nonNull = folder ?: return@consume
            nonNull.passwordAlias.takeIfNotNullOrBlank()?.let { alias ->
                viewBinding.textPasswordAlias.visibility = View.VISIBLE
                viewBinding.textPasswordAlias.text = HtmlCompat.fromHtml(
                    getString(R.string.password_alias, alias), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } ?: viewBinding.textPasswordAlias.run {
                text = null
                visibility = View.GONE
            }
        }

        viewModel.consume(viewLifecycleOwner, FolderConfirmPasswordDialogState::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textLayoutFolderPassword.error = getString(errorRes)
            } else {
                viewBinding.textLayoutFolderPassword.error = null
            }
        }

        viewModel.consume(viewLifecycleOwner, FolderConfirmPasswordDialogState::verifyPasswordSuccess) { isPasswordVerified ->
            if (isPasswordVerified) {
                dismiss()
                getCallback()?.onPasswordVerified(viewBinding.checkboxSavePassword.isChecked)
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
            getCallback()?.onCancelConfirmPassword()
        }
    }

    private fun getCallback(): OnConfirmPasswordCallback? {
        return activity?.cast() ?: parentFragment.cast()
    }
}