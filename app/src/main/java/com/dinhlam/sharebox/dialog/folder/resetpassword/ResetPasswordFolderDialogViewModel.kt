package com.dinhlam.sharebox.dialog.folder.resetpassword

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResetPasswordFolderDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository, appSharePref: AppSharePref
) : BaseViewModel<ResetPasswordFolderDialogState>(
    ResetPasswordFolderDialogState(
        passwordRecoveryHash = appSharePref.getRecoveryPassword()
    )
) {

    fun loadFolderData(folderId: String) = backgroundTask {
        val folder = folderRepository.find(folderId)
        setState { copy(folder = folder) }
    }

    fun resetFolderPassword(
        passwordRecovery: String, newPassword: String, newPasswordConfirm: String
    ) = execute { state ->
        if (passwordRecovery.isBlank()) {
            return@execute setState { copy(error = ResetPasswordFolderDialogState.Error.RequireInputPasswordRecovery) }
        }

        if (passwordRecovery != state.passwordRecoveryHash) {
            return@execute setState { copy(error = ResetPasswordFolderDialogState.Error.PasswordRecoveryIncorrect) }
        }

        if (newPassword.isBlank()) {
            return@execute setState { copy(error = ResetPasswordFolderDialogState.Error.RequireInputPassword) }
        }

        if (newPasswordConfirm.isBlank()) {
            return@execute setState { copy(error = ResetPasswordFolderDialogState.Error.RequireInputPasswordConfirm) }
        }

        if (newPassword != newPasswordConfirm) {
            return@execute setState { copy(error = ResetPasswordFolderDialogState.Error.ConfirmPasswordNotSame) }
        }

        val folder = state.folder ?: return@execute
        val newFolder = folder.copy(password = newPassword.md5())
        val success = folderRepository.update(newFolder)
        if (!success) {
            setState { copy(error = ResetPasswordFolderDialogState.Error.UnknownError) }
        } else {
            setState { copy(changePasswordSuccess = true) }
        }
    }

    fun clearError() = getState { state ->
        if (state.error != ResetPasswordFolderDialogState.Error.NoError) {
            setState { copy(error = ResetPasswordFolderDialogState.Error.NoError) }
        }
    }
}
