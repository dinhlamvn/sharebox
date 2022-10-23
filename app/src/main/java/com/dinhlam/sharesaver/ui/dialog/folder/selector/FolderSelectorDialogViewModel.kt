package com.dinhlam.sharesaver.ui.dialog.folder.selector

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderSelectorDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<FolderSelectorDialogData>(FolderSelectorDialogData()) {

    fun onSelectedFolder(position: Int) = runWithData { data ->
        val selectedFolder = data.folders.getOrNull(position) ?: return@runWithData
        setData { copy(selectedFolder = selectedFolder) }
    }

    fun requestCreateNewFolder() = setData {
        copy(requestCreateFolder = true)
    }

    init {
        executeJob {
            val folders = folderRepository.getAll()
            setData { copy(folders = folders, isFirstLoad = false) }
        }
    }
}