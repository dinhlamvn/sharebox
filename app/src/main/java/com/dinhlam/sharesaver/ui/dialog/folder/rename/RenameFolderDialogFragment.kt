package com.dinhlam.sharesaver.ui.dialog.folder.rename

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.base.BaseViewModelDialogFragment
import com.dinhlam.sharesaver.databinding.DialogFolderInputRenameBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.utils.ExtraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RenameFolderDialogFragment :
    BaseViewModelDialogFragment<RenameFolderDialogState, RenameFolderDialogViewModel, DialogFolderInputRenameBinding>() {

    interface OnConfirmRenameCallback {
        fun onRenameSuccess()
        fun onCancelRename()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderInputRenameBinding {
        return DialogFolderInputRenameBinding.inflate(inflater, container, false)
    }

    override val viewModel: RenameFolderDialogViewModel by viewModels()

    override fun onStateChanged(data: RenameFolderDialogState) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return dismiss()
        viewModel.loadFolderData(folderId)

        viewModel.consume(viewLifecycleOwner, RenameFolderDialogState::folder) { folder ->
            val nonNull = folder ?: return@consume
            viewBinding.textInputFolderName.setText(nonNull.name)
        }

        viewModel.consume(viewLifecycleOwner, RenameFolderDialogState::error) { errorRes ->
            if (errorRes != 0) {
                viewBinding.textInputFolderName.error = getString(errorRes)
            } else {
                viewBinding.textInputFolderName.error = null
            }
        }

        viewModel.consume(viewLifecycleOwner, RenameFolderDialogState::renameFolderSuccess) { isRenameFolderSuccess ->
            if (isRenameFolderSuccess) {
                dismiss()
                getCallback()?.onRenameSuccess()
            }
        }

        viewModel.consume(viewLifecycleOwner, RenameFolderDialogState::isIgnoreRename) { isIgnoreRename ->
            if (isIgnoreRename) {
                dismiss()
            }
        }

        viewBinding.textInputFolderName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.clearError()
            }
        }

        viewBinding.textInputFolderName.doAfterTextChanged {
            viewModel.clearError()
        }

        viewBinding.buttonDone.setOnClickListener {
            val newFolderName = viewBinding.textInputFolderName.getTrimmedText()
            viewModel.confirmName(newFolderName)
        }
    }

    override fun getSpacing(): Int {
        return 32
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val isVerified = withState(viewModel) { it.renameFolderSuccess }
        if (!isVerified) {
            getCallback()?.onCancelRename()
        }
    }

    private fun getCallback(): OnConfirmRenameCallback? {
        return activity?.cast() ?: parentFragment.cast()
    }
}