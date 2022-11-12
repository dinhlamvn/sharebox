package com.dinhlam.sharesaver.dialog.folder.selector

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderSelectorDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<FolderSelectorDialogState>(FolderSelectorDialogState()) {

    fun onSelectedFolder(position: Int) = withState { data ->
        val selectedFolder = data.folders.getOrNull(position) ?: return@withState
        setState { copy(selectedFolder = selectedFolder) }
    }

    fun requestCreateNewFolder() = setState {
        copy(requestCreateFolder = true)
    }

    init {
        executeJob {
            val folders = folderRepository.getAll()
            setState { copy(folders = folders, isFirstLoad = false) }
        }
    }
}