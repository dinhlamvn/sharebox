package com.dinhlam.sharesaver.dialog.folder.creator

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.extensions.md5
import com.dinhlam.sharesaver.repository.FolderRepository
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
        folderPasswordAlias: String? = null
    ) {
        if (folderName.isBlank()) {
            return setState { copy(error = R.string.error_require_folder_name) }
        }
        val folder = Folder(
            id = "folder_${System.currentTimeMillis()}",
            name = folderName,
            desc = folderDesc,
            password = folderPassword?.md5(),
            passwordAlias = folderPasswordAlias
        )
        executeJob(onError = {
            setState { copy(toastRes = R.string.error_create_folder_try_again) }
        }) {
            folderRepository.insert(folder)
            setState { copy(folderIdInserted = folder.id) }
        }
    }

    fun clearError() = withState { data ->
        if (data.error != 0) {
            setState { copy(error = 0) }
        }
    }

    fun clearToast() = withState { data ->
        if (data.toastRes != 0) {
            setState { copy(toastRes = 0) }
        }
    }
}