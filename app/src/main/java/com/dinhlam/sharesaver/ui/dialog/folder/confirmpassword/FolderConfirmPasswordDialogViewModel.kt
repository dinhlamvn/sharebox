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
) : BaseViewModel<FolderConfirmPasswordDialogData>(FolderConfirmPasswordDialogData()) {

    fun loadFolderData(folderId: String) = executeJob {
        val folder = folderRepository.get(folderId)
        setData { copy(folder = folder) }
    }

    fun confirmPassword(password: String) = runWithData { data ->
        if (password.isBlank()) {
            return@runWithData setData { copy(error = R.string.error_require_password) }
        }
        val folder = data.folder ?: return@runWithData
        val folderPassword = folder.password
        val inputPassword = password.md5()
        if (folderPassword != inputPassword) {
            setData { copy(error = R.string.password_incorrect) }
        } else {
            setData { copy(verifyPasswordSuccess = true) }
        }
    }

    fun clearError() = runWithData { data ->
        if (data.error != 0) {
            setData { copy(error = 0) }
        }
    }
}