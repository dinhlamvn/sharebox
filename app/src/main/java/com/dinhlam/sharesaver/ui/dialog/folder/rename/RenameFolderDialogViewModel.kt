package com.dinhlam.sharesaver.ui.dialog.folder.rename

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RenameFolderDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<RenameFolderDialogData>(RenameFolderDialogData()) {

    fun loadFolderData(folderId: String) = execute {
        val folder = folderRepository.get(folderId)
        setData { copy(folder = folder) }
    }

    fun confirmName(newName: String) = executeWithData { data ->
        if (newName.isBlank()) {
            return@executeWithData setData { copy(error = R.string.error_require_folder_name) }
        }
        val folder = data.folder ?: return@executeWithData
        if (newName == folder.name) {
            return@executeWithData setData { copy(isIgnoreRename = true) }
        }
        val newFolder = folder.copy(name = newName)
        val updated = folderRepository.update(newFolder)
        if (!updated) {
            setData { copy(error = R.string.error_folder_rename) }
        } else {
            setData { copy(renameFolderSuccess = true) }
        }
    }

    fun clearError() = runWithData { data ->
        if (data.error != 0) {
            setData { copy(error = 0) }
        }
    }
}