package com.dinhlam.sharebox.dialog.folder.creator

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderCreatorDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<FolderCreatorDialogState>(FolderCreatorDialogState()) {

    fun createFolder(
        folderName: String,
        folderDesc: String? = null,
        folderPassword: String? = null,
        folderPasswordReminder: String? = null
    ) {
        if (folderName.isBlank()) {
            return setState { copy(error = R.string.error_require_folder_name) }
        }
        val folder = Folder(
            id = "folder_${System.currentTimeMillis()}",
            name = folderName,
            desc = folderDesc,
            password = folderPassword?.md5(),
            passwordAlias = folderPasswordReminder
        )
        backgroundTask(onError = {
            setState { copy(toastRes = R.string.error_create_folder_try_again) }
        }) {
            folderRepository.insert(folder)
            setState { copy(folderIdInserted = folder.id) }
        }
    }

    fun clearError() = getState { state ->
        if (state.error != 0) {
            setState { copy(error = 0) }
        }
    }

    fun clearToast() = getState { state ->
        if (state.toastRes != 0) {
            setState { copy(toastRes = 0) }
        }
    }
}
