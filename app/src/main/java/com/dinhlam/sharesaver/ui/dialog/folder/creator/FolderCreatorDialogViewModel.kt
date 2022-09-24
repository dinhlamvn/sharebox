package com.dinhlam.sharesaver.ui.dialog.folder.creator

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
) : BaseViewModel<FolderCreatorDialogData>(FolderCreatorDialogData()) {

    fun createFolder(
        folderName: String,
        folderDesc: String? = null,
        folderPassword: String? = null,
        folderPasswordAlias: String? = null
    ) {
        if (folderName.isBlank()) {
            return setData { copy(error = R.string.error_require_folder_name) }
        }
        val folder = Folder(
            id = "folder_${System.currentTimeMillis()}",
            name = folderName,
            desc = folderDesc,
            password = folderPassword?.md5(),
            passwordAlias = folderPasswordAlias
        )
        execute(onError = {
            setData { copy(toastRes = R.string.error_create_folder_try_again) }
        }) {
            folderRepository.insert(folder)
            setData { copy(folderIdInserted = folder.id) }
        }
    }

    fun clearError() = runWithData { data ->
        if (data.error != 0) {
            setData { copy(error = 0) }
        }
    }

    fun clearToast() = runWithData { data ->
        if (data.toastRes != 0) {
            setData { copy(toastRes = 0) }
        }
    }
}