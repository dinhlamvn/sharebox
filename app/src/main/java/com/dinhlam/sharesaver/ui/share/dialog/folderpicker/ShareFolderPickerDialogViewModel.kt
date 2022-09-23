package com.dinhlam.sharesaver.ui.share.dialog.folderpicker

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareFolderPickerDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : BaseViewModel<ShareFolderPickerDialogData>(ShareFolderPickerDialogData()) {

    fun onSelectedFolder(position: Int) = runWithData { data ->
        val selectedFolder = data.folders.getOrNull(position) ?: return@runWithData
        setData { copy(selectedFolder = selectedFolder) }
    }

    fun requestCreateNewFolder() = setData {
        copy(requestCreateFolder = true)
    }

    init {
        execute {
            val folders = folderRepository.getAll()
            setData { copy(folders = folders, isFirstLoad = false) }
        }
    }
}