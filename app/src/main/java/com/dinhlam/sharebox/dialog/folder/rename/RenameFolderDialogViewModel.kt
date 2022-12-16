package com.dinhlam.sharebox.dialog.folder.rename

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RenameFolderDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<RenameFolderDialogState>(RenameFolderDialogState()) {

    fun loadFolderData(folderId: String) = executeJob {
        val folder = folderRepository.get(folderId)
        setState { copy(folder = folder) }
    }

    fun confirmName(newName: String) = execute { data ->
        if (newName.isBlank()) {
            return@execute setState { copy(error = R.string.error_require_folder_name) }
        }
        val folder = data.folder ?: return@execute
        if (newName == folder.name) {
            return@execute setState { copy(isIgnoreRename = true) }
        }
        val newFolder = folder.copy(name = newName)
        val updated = folderRepository.update(newFolder)
        if (!updated) {
            setState { copy(error = R.string.error_folder_rename) }
        } else {
            setState { copy(renameFolderSuccess = true) }
        }
    }

    fun clearError() = withState { data ->
        if (data.error != 0) {
            setState { copy(error = 0) }
        }
    }
}
