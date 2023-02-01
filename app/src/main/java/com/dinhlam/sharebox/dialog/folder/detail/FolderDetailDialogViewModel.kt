package com.dinhlam.sharebox.dialog.folder.detail

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderDetailDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<FolderDetailDialogState>(FolderDetailDialogState()) {

    fun loadFolderData(folderId: String) = backgroundTask {
        val folder = folderRepository.find(folderId)
        val shareCount = shareRepository.countByFolder(folderId)
        setState { copy(folder = folder, shareCount = shareCount) }
    }
}
