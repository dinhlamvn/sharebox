package com.dinhlam.sharebox.dialog.folder.resetpassword

import androidx.annotation.StringRes
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class ResetPasswordFolderDialogState(
    val folder: Folder? = null,
    val error: Error = Error.NoError,
    @StringRes val toastRes: Int = 0,
    val changePasswordSuccess: Boolean = false,
    val passwordRecoveryHash: String
) : BaseViewModel.BaseState {

    enum class Error(@StringRes val errorMessageRes: Int = 0) {
        NoError(0),
        RequireInputPasswordRecovery(R.string.error_require_password_recovery_hash),
        RequireInputPassword(R.string.error_require_password),
        RequireInputPasswordConfirm(R.string.error_require_password_confirm),
        PasswordRecoveryIncorrect(R.string.error_password_recovery_incorrect),
        ConfirmPasswordNotSame(R.string.error_confirm_password_not_same),
        UnknownError(R.string.error_change_password_unknown)
    }
}
