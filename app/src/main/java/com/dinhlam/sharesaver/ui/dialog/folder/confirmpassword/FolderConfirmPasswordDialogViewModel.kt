package com.dinhlam.sharesaver.ui.dialog.folder.confirmpassword

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.extensions.md5
import com.dinhlam.sharesaver.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderConfirmPasswordDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<FolderConfirmPasswordDialogState>(FolderConfirmPasswordDialogState()) {

    fun loadFolderData(folderId: String) = executeJob {
        val folder = folderRepository.get(folderId)
        setState { copy(folder = folder) }
    }

    fun confirmPassword(password: String) = withState { data ->
        if (password.isBlank()) {
            return@withState setState { copy(error = R.string.error_require_password) }
        }
        val folder = data.folder ?: return@withState
        val folderPassword = folder.password
        val inputPassword = password.md5()
        if (folderPassword != inputPassword) {
            setState { copy(error = R.string.password_incorrect) }
        } else {
            setState { copy(verifyPasswordSuccess = true) }
        }
    }

    fun clearError() = withState { data ->
        if (data.error != 0) {
            setState { copy(error = 0) }
        }
    }
}